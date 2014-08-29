package com.amphinicy.blink.rootbeer;

import static com.google.zxing.common.reedsolomon.GenericGF.QR_CODE_FIELD_256;
import com.google.zxing.common.reedsolomon.ReedSolomonEncoder;
import org.junit.Assert;
import org.trifort.rootbeer.runtime.Kernel;
import org.trifort.rootbeer.runtime.Rootbeer;

import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.System.currentTimeMillis;

public class RootBeerDemo {

    public static void main(String[] args) {
        int numBlocks = Integer.parseInt(args[0]);

        System.out.println("Starting application. numBlocks: " + numBlocks);
        final ReedSolomonEncoder encoder = new ReedSolomonEncoder(QR_CODE_FIELD_256);
        final int[][] data = new int[numBlocks][255];
        final int[][] original = new int[numBlocks][];

        System.out.println("Creating and encoding block");
        int value = 0;
        for (int i = 0; i < numBlocks; i++) {
            int[] block = data[i];
            for (int j = 0; j < 239; j++) {
                block[j] = value;
                value = (value + 1) % 256;
            }
            encoder.encode(block, 16);   // Commenting out this line gives an out of memory error for numBlocks >= 7128 on my GeForce GT 525M
            original[i] = Arrays.copyOf(block, block.length);

            // Corrupt a byte
            block[50] ^= 44;
        }

        System.out.println("Decoding block ...");
        long start = currentTimeMillis();

        ArrayList<Kernel> kernels = new ArrayList<Kernel>(numBlocks);
        for (int[] block : data) {
            kernels.add(new RsKernel(block));
        }

        Rootbeer beer = new Rootbeer();
        beer.run(kernels);

        long duration = currentTimeMillis() - start;
        System.out.println("Decoding block took " + duration + " ms.");

        Assert.assertArrayEquals(original, data);
        System.out.println("Data decoded successfully.");
    }

    public static class RsKernel implements Kernel {
        private final int[] block;

        public RsKernel(int[] block) {
            this.block = block;
        }

        @Override
        public void gpuMethod() {
            block[50] ^= 44;
        }
    }
}

