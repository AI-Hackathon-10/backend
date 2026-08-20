package com.ktbaihackathon.symptom.service;

import com.ktbaihackathon.common.exception.CustomException;
import com.ktbaihackathon.common.response.ResultCode;
import com.ktbaihackathon.symptom.dto.SymptomRecordCreateRequest;
import com.ktbaihackathon.symptom.entity.SymptomRecord;
import com.ktbaihackathon.symptom.repository.SymptomRecordRepository;
import com.ktbaihackathon.user.entity.User;
import com.ktbaihackathon.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SymptomRecordService {

    private final SymptomRecordRepository symptomRecordRepository;
    private final UserRepository userRepository;

    @Transactional
    public SymptomRecord create(Long userId, SymptomRecordCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ResultCode.INVALID_TOKEN));

        SymptomRecord symptomRecord = SymptomRecord.create(
                user,
                request.startedAt(),
                request.memo()
        );

        request.symptomTypes().forEach(symptomRecord::addSymptom);

        return symptomRecordRepository.save(symptomRecord);
    }
}
