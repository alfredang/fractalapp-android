import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** Play Store feature graphic (1024x500): a Julia render on the right, a violet
 *  scrim + app name/tagline on the left. */
public class FeatureGen {
    static int[] cosColor(double t) {
        double[] a={0.55,0.50,0.55}, b={0.45,0.45,0.45}, c={1,1,1}, d={0.05,0.35,0.65};
        int[] o=new int[3];
        for(int i=0;i<3;i++){ double v=a[i]+b[i]*Math.cos(6.28318530718*(c[i]*t+d[i]));
            v=v/(v+0.85); v=Math.pow(Math.max(0,Math.min(1,v)),0.85);
            o[i]=(int)Math.round(Math.max(0,Math.min(1,v))*255);} return o;
    }
    public static void main(String[] a) throws Exception {
        int W=1024,H=500;
        BufferedImage img=new BufferedImage(W,H,BufferedImage.TYPE_INT_RGB);
        // Julia background spanning full width
        double cx=-0.8, cy=0.156, escape=64.0; int maxIter=260;
        double spanY=1.4, spanX=spanY*W/(double)H;
        for(int y=0;y<H;y++) for(int x=0;x<W;x++){
            double zx=(x/(double)W*2-1)*spanX + 0.2;
            double zy=(y/(double)H*2-1)*spanY;
            int i=0; while(i<maxIter){ double xt=zx*zx-zy*zy+cx; zy=2*zx*zy+cy; zx=xt; if(zx*zx+zy*zy>escape*escape) break; i++; }
            int[] col; if(i>=maxIter) col=new int[]{14,7,28};
            else { double mag=Math.max(Math.sqrt(zx*zx+zy*zy),1.0001);
                double nu=Math.log(Math.log(mag)/Math.log(escape))/Math.log(2.0);
                col=cosColor((i+1.0-nu)*0.022+0.08);}
            img.setRGB(x,y,(col[0]<<16)|(col[1]<<8)|col[2]);
        }
        Graphics2D g=img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        // Left scrim for text legibility
        g.setPaint(new GradientPaint(0,0,new Color(0x12,0x07,0x26,235), W*0.62f,0,new Color(0x12,0x07,0x26,0)));
        g.fillRect(0,0,W,H);
        // Title + tagline
        g.setColor(Color.WHITE);
        g.setFont(new Font("Helvetica Neue",Font.BOLD,84));
        g.drawString("Fractal",70,250);
        g.setColor(new Color(0xE6,0xD8,0xFF));
        g.setFont(new Font("Helvetica Neue",Font.PLAIN,34));
        g.drawString("Generate colorful fractal art",72,305);
        g.setColor(new Color(0xC1,0x6B,0xFF));
        g.setFont(new Font("Helvetica Neue",Font.PLAIN,26));
        g.drawString("Mandelbrot · Julia · Burning Ship · and more",72,352);
        g.dispose();
        File f=new File(a[0]); f.getParentFile().mkdirs();
        ImageIO.write(img,"png",f); System.out.println("wrote "+a[0]);
    }
}
