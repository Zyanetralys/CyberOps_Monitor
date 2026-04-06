package com.zyanetralys.cyberops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@RestController
@CrossOrigin(origins = "*")
public class CyberOpsBackend {

    public static void main(String[] args) { SpringApplication.run(CyberOpsBackend.class, args); }

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private static final Random RAND = new Random();
    private static final String[] BOTS = {"NullPtr","CipherWolf","KernelPanic","PhantomRoot","ZeroTrust","DarkNode"};
    private static final String[] ROLES = {"SOC L2","Threat Hunter","Red Team","Reverse Eng","Cloud Sec","IR Lead"};
    private static final String[] COUNTRIES = {"US","CN","RU","IR","KP","DE","ES","BR","JP","AU"};
    private static final String[] ATTACKS = {"DDoS","C2 Beacon","SQLi","XSS","Malware Drop","Zero-Day","Data Exfil"};
    private static final String[] KEYWORDS = {"c2","malware","edr","zero-day","yara","pentest","exploit","ransomware"};

    @GetMapping("/api/connect")
    public Map<String, String> connect() {
        return Map.of("sessionId", "zya-" + UUID.randomUUID().toString().substring(0,4), "status", "connected");
    }

    @GetMapping(value = "/api/stream/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable String id) {
        SseEmitter emitter = new SseEmitter(60000L);
        scheduler.scheduleAtFixedRate(() -> {
            try {
                Map<String, Object> event = RAND.nextDouble() < 0.65 ? generateChat() : generateAttack();
                emitter.send(SseEmitter.event().data(event));
            } catch (IOException e) { emitter.complete(); }
        }, 0, 750, TimeUnit.MILLISECONDS);
        return emitter;
    }

    private Map<String, Object> generateChat() {
        Map<String, String> bot = Map.of(
            "id", "B"+RAND.nextInt(6)+1,
            "handle", BOTS[RAND.nextInt(BOTS.length)],
            "role", ROLES[RAND.nextInt(ROLES.length)]
        );
        String kw = KEYWORDS[RAND.nextInt(KEYWORDS.length)];
        Map<String, Object> msg = Map.of(
            "text", "scanning "+kw+" artifacts, updating EDR rules",
            "replyTarget", RAND.nextBoolean() ? "@"+BOTS[RAND.nextInt(BOTS.length)] : null,
            "kw", kw
        );
        return Map.of("type", "chat", "bot", bot, "msg", msg, "ts", Instant.now().toString());
    }

    private Map<String, Object> generateAttack() {
        return Map.of(
            "type", "attack",
            "src", COUNTRIES[RAND.nextInt(4)],
            "tgt", COUNTRIES[4 + RAND.nextInt(6)],
            "attackType", ATTACKS[RAND.nextInt(ATTACKS.length)],
            "severity", RAND.nextDouble() > 0.75 ? "crit" : "high",
            "detected", RAND.nextBoolean(),
            "progress", 0.0,
            "ts", Instant.now().toString()
        );
    }
}
