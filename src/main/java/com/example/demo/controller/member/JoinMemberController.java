package com.example.demo.controller.member;

import com.example.demo.dto.member.MemberRequest.*;
import com.example.demo.service.email.EmailService;
import com.example.demo.service.member.MemberService;
import com.example.demo.service.email.JoinEmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/member")
@Validated
@Tag(name = "회원가입 API") // 해당 클래스의 역할을 설명
public class JoinMemberController {

    private final MemberService memberService;
    private final EmailService emailService;
    private final JoinEmailService joinEmailService;


    @PostMapping
    @Operation(summary = "회원가입")
    @ApiResponses(
        value = {
                @ApiResponse(responseCode = "201", description = "유저 생성 완료"),
                @ApiResponse(responseCode = "400", description = "잘못된 입력 값"),
                @ApiResponse(responseCode = "401", description = "인증되지 않은 이메일")
        }
    )
    public ResponseEntity<String> save(@Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
                                       @Valid @RequestBody CreateMember takenDto,
                                       HttpServletRequest request) {

        String ip = request.getRemoteAddr();
        log.info("{}: 유저 생성 API 호출", ip);

        if(!joinEmailService.checkAuthedEmailForJoin(takenDto.email()))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증되지 않은 이메일입니다");

        memberService.addUser(takenDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("생성이 완료되었습니다");
    }

    @PostMapping("/confirm")
    @Operation(summary = "아이디 및 닉네임 중복 검사")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "중복되지 않음"),
                    @ApiResponse(responseCode = "400", description = "입력 값이 잘못됨"),
                    @ApiResponse(responseCode = "409", description = "중복됨")
            }
    )
    public ResponseEntity<String> checkDuplicated(@Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
                                                  @Valid @RequestBody DataConfirmMember takenDto,
                                                  HttpServletRequest request) {

        String ip = request.getRemoteAddr();
        log.info("{}: 중복 검사 API 호출", ip);

        if(takenDto.username() != null && !memberService.checkDuplicatedUsername(takenDto.username()))
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용 중인 아이디입니다");

        if(takenDto.nickname() != null && !memberService.checkDuplicatedNickname(takenDto.nickname()))
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용 중인 닉네임입니다");

        return ResponseEntity.ok("중복되지 않았습니다");
    }

    @GetMapping("/emailAuthed/{email}")
    @Operation(summary = "이메일 중복 확인 및 회원가입 인증 이메일 발송")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "인증 이메일 전송 완료"),
                    @ApiResponse(responseCode = "400", description = "입력 값이 잘못됨"),
                    @ApiResponse(responseCode = "409", description = "이메일이 중복 중복됨")
            }
    )
    public ResponseEntity<String> emailAuthed(@NotBlank @Size(min = 1, max = 50) @Schema(example = "example@naver.com")
                                              @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
                                                       message = "유효한 이메일이 아닙니다.")
                                              @PathVariable("email") String email,
                                              HttpServletRequest request) {

        String ip = request.getRemoteAddr();
        log.info("{}: 이메일 인증코드 전송 API 호출", ip);

        if(!memberService.checkDuplicatedEmail(email))
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용 중인 이메일 입니다");

        String authedKey = joinEmailService.createAuthedKeyForJoin();
        String title = "회원가입 이메일 인증 코드";
        String text = "이메일 인증 코드: " + authedKey;

        emailService.sendEmail(email, title, text, authedKey);
        return ResponseEntity.ok("이메일 인증 코드 전송 완료");
    }

    @PostMapping("/emailAuthed/{email}")
    @Operation(summary = "이메일 인증코드 확인")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "이메일 인증 성공"),
                    @ApiResponse(responseCode = "401", description = "이메일 인증 실패")
            }
    )
    public ResponseEntity<String> emailAuthedCheck(@Size(min = 1, max = 50) @Schema(example = "example@naver.com")
                                                   @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
                                                           message = "유효한 이메일이 아닙니다.")
                                                   @PathVariable("email") String email,

                                                   @NotBlank @Schema(example = "1234")
                                                   @Pattern(regexp = "^\\d{4}$", message = "인증 코드는 4자리 숫자입니다.")
                                                   @RequestBody String key,

                                                   HttpServletRequest request) {

        String ip = request.getRemoteAddr();
        log.info("{}: 이메일 인증 API 호출", ip);

        return joinEmailService.checkAuthedKeyForJoin(email, key) ?
                ResponseEntity.ok("이메일 인증에 성공하였습니다") :
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("이메일 인증에 실패하였습니다");
    }
}