package com.bigzhao.ecode;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by yangzhao.lyz on 2015/9/15.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        String s= FileUtils.readFileToString(new File("D:/test/aaa.js"));
        JsRefactor jr=new JsRefactor();
        jr.refactor(s,"name");
    }
}
