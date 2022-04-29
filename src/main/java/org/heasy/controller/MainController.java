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

    //key name value sort
    private static final Map<String, Integer> MAP = new HashMap<>(8);

    //key name value sessionId
    private static final BiMap<String, String> SESSION_MAP = new BiMap<>(new HashMap<>(8));
    private static volatile int ng = RandomUtil.randomInt(1, 5);
    private static final AtomicInteger SORT = new AtomicInteger(0);
    private static String lastNeiGui = null;
    private static DateTime lastDate = DateTime.of(System.currentTimeMillis());

    private static String owner = null;
    private static String ownerSessionId = null;

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
                return ResponseEntity.ok(JSONUtil.toJsonStr(MAP) + getInfo(name));
            }
        } else {
            if (SESSION_MAP.size() >= 5) {
                return ResponseEntity.ok(JSONUtil.toJsonStr(MAP) + "人数已满");
            }
            SESSION_MAP.put(name, id);
            final int i = SORT.addAndGet(1);
            //第一个进来的 设置为owner
            if (i == 1) {
                owner = name;
            }
            MAP.put(name, i);
            return ResponseEntity.ok(JSONUtil.toJsonStr(MAP) + getInfo(name));
        }
    }

    @GetMapping("/restart")
    public ResponseEntity<?> start(HttpServletRequest request) {
        final String id = request.getSession().getId();
        if (!StrUtil.equals(id, ownerSessionId)) {
            return ResponseEntity.ok("你没有重开权限 当前局管理员是:" + owner);
        }
        if (!SESSION_MAP.isEmpty()) {
            return ResponseEntity.ok("还有人未投票：" + JSONUtil.toJsonStr(SESSION_MAP.keySet()));
        }
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
        owner = null;
        ownerSessionId = null;
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

    @GetMapping("/vote")
    public ResponseEntity<?> volt(@RequestParam(required = false) String neigui, HttpServletRequest request) {
        if (StrUtil.isBlank(neigui) || !MAP.containsKey(neigui)) {
            return ResponseEntity.ok(JSONUtil.toJsonStr(MAP) + "  你输的名称不存在奥");
        }
        final String sessionId = request.getSession().getId();
        final String me = SESSION_MAP.getKey(sessionId);
        if (me == null) {
            return ResponseEntity.ok("你已经投过票了奥");
        }
        if (StrUtil.equals(me, neigui)) {
            return ResponseEntity.ok("不能投自己 你可真是个大聪明");
        }
        //投过票了 移除
        SESSION_MAP.remove(me);

        final Integer integer = MAP.get(neigui);
        if (integer != null && integer == ng) {
            return ResponseEntity.ok(DateTime.now() + JSONUtil.toJsonStr(MAP) + " 恭喜你抓到了内鬼!");
        } else {
            return ResponseEntity.ok(DateTime.now() + JSONUtil.toJsonStr(MAP) + " 猜错了奥");
        }
    }

    private static synchronized String getInfo(String name) {
        final Integer integer = MAP.get(name);
        if (integer == null) {
            return "error";
        }
        return integer == ng ? "你是内鬼!!!" : "你是平民";
    }
}
