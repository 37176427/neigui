package org.heasy.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.map.BiMap;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wyh
 * @date 2022/4/29 16:13
 **/
@RestController
@RequestMapping("/neigui")
@Slf4j
public class MainController {

    private static final Map<String, Integer> MAP = new HashMap<>(8);
    private static final BiMap<String, String> SESSION_MAP = new BiMap<>(new HashMap<>(8));
    private static volatile int ng = RandomUtil.randomInt(1, 5);
    private static final AtomicInteger SORT = new AtomicInteger(0);
    private static String lastNeiGui = null;
    private static DateTime lastDate = DateTime.of(System.currentTimeMillis());

    @GetMapping("/whoami")
    public ResponseEntity<?> whoami(@RequestParam(required = false) String name, HttpServletRequest request, HttpServletResponse response) {
        if (StrUtil.isBlank(name)) {
            return ResponseEntity.ok("给自己起个名OK?");
        }
        final String id = request.getSession().getId();
        final String key = SESSION_MAP.getKey(id);
        if (key != null) {
            if (!StrUtil.equals(key, name)) {
                return ResponseEntity.ok("你已经是:" + key + ",别瞎JB刷");
            }
        }

        //名称重复时
        if (SESSION_MAP.containsKey(name)) {
            final String sessionId = SESSION_MAP.get(name);
            if (!StrUtil.equals(sessionId, id)) {
                return ResponseEntity.ok("名称已被占用");
            } else {
                return ResponseEntity.ok(getInfo(name));
            }
        } else {
            if (SESSION_MAP.size() >= 5) {
                return ResponseEntity.ok("人数已满");
            }
            SESSION_MAP.put(name, id);
            MAP.put(name, SORT.addAndGet(1));
            return ResponseEntity.ok(getInfo(name));
        }
    }

    @GetMapping("/restart")
    public ResponseEntity<?> start(HttpServletRequest request) {
        final String id = request.getSession().getId();
        final String key = SESSION_MAP.getKey(id);
        log.info(StrUtil.nullToDefault(key, "未知用户") + "调用restart");
        for (Map.Entry<String, Integer> entry : MAP.entrySet()) {
            if (entry.getValue() == ng) {
                lastNeiGui = entry.getKey();
                break;
            }
        }
        ng = RandomUtil.randomInt(1, 5);
        MAP.clear();
        SESSION_MAP.clear();
        SORT.set(0);
        lastDate = DateTime.of(System.currentTimeMillis());
        return neigui();
    }

    @GetMapping("/who")
    public ResponseEntity<?> neigui() {
        return ResponseEntity.ok(lastNeiGui == null ? "无内鬼" : lastDate.toString() + "内鬼是:" + lastNeiGui);
    }

    @GetMapping("/list")
    public ResponseEntity<?> list() {
        final String s = JSONUtil.toJsonStr(MAP);
        return ResponseEntity.ok(s);
    }

    private static synchronized String getInfo(String name) {
        final Integer integer = MAP.get(name);
        if (integer == null) {
            return "error";
        }
        return integer == ng ? "你是内鬼!!!" : "你是平民";
    }
}
