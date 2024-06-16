package mmc.extentions.setupSpriteGenerationTask;

import arc.files.*;
import arc.graphics.*;

import java.util.*;

public class FileAntialiasing {

    static int getRGB(Pixmap image, int ix, int iy) {
        return image.getRaw(Math.max(Math.min(ix, image.width - 1), 0), Math.max(Math.min(iy, image.height - 1), 0));
    }
    public static void antialias(Fi file){
        Pixmap image = new Pixmap(file);
        Pixmap out = image.copy();

        Color color = new Color();
        Color sum = new Color();
        Color suma = new Color();
        int[] p = new int[9];

        for(int x = 0; x < image.width; x++){
            for(int y = 0; y < image.height; y++){
                int A = getRGB(image, x - 1, y + 1),
                        B = getRGB(image, x, y + 1),
                        C = getRGB(image, x + 1, y + 1),
                        D = getRGB(image, x - 1, y),
                        E = getRGB(image, x, y),
                        F = getRGB(image, x + 1, y),
                        G = getRGB(image, x - 1, y - 1),
                        H = getRGB(image, x, y - 1),
                        I = getRGB(image, x + 1, y - 1);

                Arrays.fill(p, E);

                if(D == B && D != H && B != F) p[0] = D;
                if((D == B && D != H && B != F && E != C) || (B == F && B != D && F != H && E != A)) p[1] = B;
                if(B == F && B != D && F != H) p[2] = F;
                if((H == D && H != F && D != B && E != A) || (D == B && D != H && B != F && E != G)) p[3] = D;
                if((B == F && B != D && F != H && E != I) || (F == H && F != B && H != D && E != C)) p[5] = F;
                if(H == D && H != F && D != B) p[6] = D;
                if((F == H && F != B && H != D && E != G) || (H == D && H != F && D != B && E != I)) p[7] = H;
                if(F == H && F != B && H != D) p[8] = F;

                suma.set(0);

                for(int val : p){
                    color.rgba8888(val);
                    color.premultiplyAlpha();
                    suma.r(suma.r + color.r);
                    suma.g(suma.g + color.g);
                    suma.b(suma.b + color.b);
                    suma.a(suma.a + color.a);
                }

                float fm = suma.a <= 0.001f ? 0f : (float)(1f / suma.a);
                suma.mul(fm, fm, fm, fm);

                float total = 0;
                sum.set(0);

                for(int val : p){
                    color.rgba8888(val);
                    float a = color.a;
                    color.lerp(suma, (float) (1f - a));
                    sum.r(sum.r + color.r);
                    sum.g(sum.g + color.g);
                    sum.b(sum.b + color.b);
                    sum.a(sum.a + a);
                    total += 1f;
                }

                fm = (float)(1f / total);
                sum.mul(fm, fm, fm, fm);
                out.setRaw(x, y, sum.rgba8888());
                sum.set(0);
            }
        }

        image.dispose();
        out.dispose();

        file.writePng(out);
    }
}
