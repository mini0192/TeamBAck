package com.example.demo.member.email.application;

import com.example.demo.config.redis.RedisIdentifier;
import com.example.demo.config.redis.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

/**
 * 회원가입에 대한 Email 전송
 */
@Service
@RequiredArgsConstructor
public class JoinMemberEmailService {

    private final EmailService emailService;
    private final RedisRepository redisRepository;

    private final String REDIS_IDENTIFIER = RedisIdentifier.EMAIL.getIdentifier();
    private final String AUTHED = "Authed";
    private final int VALID_TIME = 5;

    // ToDo: 전송 실패 시 처리 안해줌
    public void sendAuthedEmail(String email, String authedKey) {
        String title = "회원가입 이메일 인증 코드";
        String text = "이메일 인증 코드: " + authedKey;

        redisRepository.setData(REDIS_IDENTIFIER + email, authedKey, Duration.ofMinutes(VALID_TIME));
        emailService.sendEmail(email, title, text);
    }

    // 회원가입에 인증 이메일의 Key 값과 해당 이메일에 발송 된 Key 값이 같다면 해당 이메일의 값을 Authed로 변경
    public boolean checkAuthedKey(String email, String authedKey) {
        String savedAuthedKey = (String) redisRepository.getDataByKey(REDIS_IDENTIFIER + email);
        if(savedAuthedKey == null) return false;
        if(!savedAuthedKey.equals(authedKey)) return false;

        redisRepository.setData(REDIS_IDENTIFIER + email, AUTHED, Duration.ofMinutes(VALID_TIME));
        return true;
    }

    // 회원가입 인증 이메일의 값이 Authed라면 인증된 사용자라는 것으로 간주함
    public boolean checkAuthedEmail(String email) {
        String savedAuthedKey = (String) redisRepository.getDataByKey(REDIS_IDENTIFIER + email);
        if(savedAuthedKey == null) return false;
        if(!savedAuthedKey.equals(AUTHED)) return false;

        redisRepository.deleteKey(REDIS_IDENTIFIER + email);
        return true;
    }

    // 회원가입 인증 이메일을 전송 할 때 임의의 4자리 수를 만들고 이 값을 Map 자료구조에 저장
    public String createAuthedKey() {
        Random random = new Random();
        random.setSeed(System.currentTimeMillis());

        int rInt = random.nextInt(10000);
        return String.format("%04d", rInt);
    }
}
