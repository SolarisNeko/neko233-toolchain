package com.neko233.toolchain.compress.huffman;

import org.junit.Assert;
import org.junit.Test;

public class HuffmanAlgorithm233Test {

    @Test
    public void test_base() {
        String content = "Hello, I'm SolarisNeko~ Nice to meet you.";

        byte[] contentBytes = content.getBytes();
        HuffmanResult result = HuffmanAlgorithm233.zip(contentBytes);

        byte[] unzip = HuffmanAlgorithm233.unzip(result.getHuffmanPathTree(), result.getZip());
        Assert.assertEquals(content, new String(unzip));
    }

}