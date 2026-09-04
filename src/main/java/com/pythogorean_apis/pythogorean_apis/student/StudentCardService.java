package com.pythogorean_apis.pythogorean_apis.student;

import com.pythogorean_apis.pythogorean_apis.common.exception.BadRequestException;
import com.pythogorean_apis.pythogorean_apis.common.exception.ServiceUnavailableException;
import com.pythogorean_apis.pythogorean_apis.vision.VisionClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Service
public class StudentCardService {

    private static final Logger log = LoggerFactory.getLogger(StudentCardService.class);

    private final ArucoCardRepository arucoCardRepository;
    private final VisionClient visionClient;

    public StudentCardService(
            ArucoCardRepository arucoCardRepository,
            VisionClient visionClient) {

        this.arucoCardRepository = arucoCardRepository;
        this.visionClient = visionClient;
    }

    public Optional<ArucoCardEntity> findFor(Long studentId) {
        return arucoCardRepository.findByStudentId(studentId);
    }

    public ArucoCardEntity generateFor(StudentEntity student) {

        byte[] image = render(student);

        ArucoCardEntity card = arucoCardRepository.findByStudentId(student.getId())
                .orElseGet(ArucoCardEntity::new);

        card.setStudent(student);
        card.setMarkerId(student.getArucoMarkerId());
        card.setArucoDictionary(visionClient.dictionary());
        card.setImage(image);

        return arucoCardRepository.save(card);
    }

    public boolean tryGenerateFor(StudentEntity student) {

        byte[] image;

        try {
            image = render(student);
        } catch (Exception exception) {
            log.warn(
                    "Could not render a card for student {} with marker {}; the student was saved without one",
                    student.getId(),
                    student.getArucoMarkerId(),
                    exception);
            return false;
        }

        ArucoCardEntity card = new ArucoCardEntity();
        card.setStudent(student);
        card.setMarkerId(student.getArucoMarkerId());
        card.setArucoDictionary(visionClient.dictionary());
        card.setImage(image);

        arucoCardRepository.save(card);

        return true;
    }

    private byte[] render(StudentEntity student) {

        Integer markerId = student.getArucoMarkerId();

        if (markerId == null) {
            throw new BadRequestException(
                    "This student has no ArUco card number yet, so no card can be drawn for them");
        }

        String label = student.getUser().getName();

        byte[] image;

        try {
            image = visionClient.renderCard(markerId, label);
        } catch (RestClientException exception) {
            throw new ServiceUnavailableException(
                    "The vision layer is not reachable. Start it on port 8000 and try again.");
        }

        if (image == null || image.length == 0) {
            throw new ServiceUnavailableException("The vision layer returned an empty card");
        }

        return image;
    }
}
