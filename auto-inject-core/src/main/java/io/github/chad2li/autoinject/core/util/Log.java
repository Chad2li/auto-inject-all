package io.github.chad2li.autoinject.core.util;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ArrayUtil;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import java.io.BufferedWriter;
import java.util.StringJoiner;

/**
 * 测试使用的日志输出工具
 *
 * @author chad
 * @date 2022/5/14 00:16
 * @since 1 create by chad
 */
public class Log {
    private static BufferedWriter WRITER;
    private static Messager messager;

    private static final String TAG = "DictAuto: ";

    public static void init(Messager messager) {
        Log.messager = messager;
    }

    /**
     * 输出调试测试语句
     *
     * @param msg 消息
     * @date 2022/5/19 12:22
     * @author chad
     * @since 1 by chad at 2022/5/19
     */
    public static void write(String... msg) {
        if (ArrayUtil.isEmpty(msg)) {
            return;
        }
        StringJoiner sj = new StringJoiner("\n");
        for (String s : msg) {
            sj.add(TAG + s);
        }
        Log.messager.printMessage(Diagnostic.Kind.NOTE, sj.toString());
    }

    /**
     * 输出调试信息和异常信息
     *
     * @param msg       调试信息
     * @param throwable 异常
     * @date 2022/5/19 12:22
     * @author chad
     * @since 1 by chad at 2022/5/19
     */
    public static void write(String msg, Throwable throwable) {
        write(msg);
        write(throwable);
    }

    /**
     * 输入异常信息
     *
     * @param throwable 异常
     * @date 2022/5/19 12:22
     * @author chad
     * @since 1 by chad at 2022/5/19
     */
    public static void write(Throwable throwable) {
        String errMsg = ExceptionUtil.stacktraceToString(throwable);
        write(errMsg);
    }
}
