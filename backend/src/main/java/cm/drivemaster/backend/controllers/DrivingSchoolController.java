package cm.drivemaster.backend.controllers;


import cm.drivemaster.backend.models.dto.DrivingSchoolRequestDto;
import cm.drivemaster.backend.models.dto.DrivingSchoolResponseDto;
import cm.drivemaster.backend.services.DrivingSchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/driving-schools")
@CrossOrigin(originPatterns = "*")
@RequiredArgsConstructor
public class DrivingSchoolController {

    private final DrivingSchoolService drivingSchoolService;

    @PostMapping
    public ResponseEntity<String> createSchool(@RequestBody DrivingSchoolRequestDto dto, long adminId) throws IllegalAccessException {
        drivingSchoolService.createSchool(dto, adminId);
        return ResponseEntity.status(HttpStatus.CREATED).body("auto ecole crée avec success !");
    }

    @GetMapping("/all")
    public ResponseEntity<List<DrivingSchoolResponseDto>> retreiveSchool() {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(drivingSchoolService.retreiveSchool());
    }
}
