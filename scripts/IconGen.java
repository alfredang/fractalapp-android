import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** Tertiary Fractal launcher icon — a real Julia-set medallion on a deep-violet gradient.
 *  Generates adaptive foreground PNGs (transparent outside a centred disc, kept inside the
 *  adaptive safe-zone), legacy square icons, and the Play Store 512/1024 art. */
public class IconGen {
    static final Color BG_TL = new Color(0x2A,0x0F,0x4E);
    static final Color BG_BR = new Color(0x12,0x07,0x26);

    // Cosine palette (matches the app's "Neon"-ish look) -> RGB in 0..255
    static int[] cosColor(double t) {
        double[] a={0.55,0.50,0.55}, b={0.45,0.45,0.45}, c={1,1,1}, d={0.05,0.35,0.65};
        int[] o=new int[3];
        for(int i=0;i<3;i++){
            double v=a[i]+b[i]*Math.cos(6.28318530718*(c[i]*t+d[i]));
            v=v/(v+0.85); v=Math.pow(Math.max(0,Math.min(1,v)),0.85);
            o[i]=(int)Math.round(Math.max(0,Math.min(1,v))*255);
        }
        return o;
    }

    /** Render a Julia set into an ARGB image; pixels outside `disc` are left transparent. */
    static BufferedImage juliaDisc(int size, boolean disc) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        double cx=-0.8, cy=0.156, span=1.5, escape=64.0; int maxIter=260;
        double r = size*0.46, ccx=size/2.0, ccy=size/2.0;
        for(int y=0;y<size;y++){
            for(int x=0;x<size;x++){
                if(disc){
                    double dx=x-ccx, dy=y-ccy;
                    if(dx*dx+dy*dy > r*r) continue; // transparent outside disc
                }
                double zx=(x/(double)size*2-1)*span;
                double zy=(y/(double)size*2-1)*span;
                int i=0; while(i<maxIter){ double xt=zx*zx-zy*zy+cx; zy=2*zx*zy+cy; zx=xt; if(zx*zx+zy*zy>escape*escape) break; i++; }
                int[] col;
                if(i>=maxIter){ col=new int[]{14,7,28}; }
                else{
                    double mag=Math.max(Math.sqrt(zx*zx+zy*zy),1.0001);
                    double nu=Math.log(Math.log(mag)/Math.log(escape))/Math.log(2.0);
                    double t=(i+1.0-nu)*0.022+0.08;
                    col=cosColor(t);
                }
                img.setRGB(x,y,(0xFF<<24)|(col[0]<<16)|(col[1]<<8)|col[2]);
            }
        }
        return img;
    }

    static BufferedImage foreground(int size){ return juliaDisc(size, true); }

    static BufferedImage fullIcon(int size, boolean rounded){
        BufferedImage img=new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
        Shape clip = rounded ? new RoundRectangle2D.Float(0,0,size,size,size*0.23f,size*0.23f)
                             : new Rectangle(0,0,size,size);
        g.setClip(clip);
        g.setPaint(new GradientPaint(0,0,BG_TL,size,size,BG_BR));
        g.fill(clip);
        // Julia medallion, full square fractal scaled to ~0.92 and centred (no disc clip — looks richer on the square art)
        BufferedImage j=juliaDisc(size, false);
        int inset=(int)(size*0.06);
        g.drawImage(j, inset, inset, size-2*inset, size-2*inset, null);
        g.dispose();
        return img;
    }

    static void write(BufferedImage img, String path) throws Exception {
        File f=new File(path); f.getParentFile().mkdirs();
        ImageIO.write(img,"png",f); System.out.println("wrote "+path);
    }

    public static void main(String[] a) throws Exception {
        String res=a[0];           // app/src/main/res
        String store=a[1];         // store-assets
        String[] dirs={"mipmap-mdpi","mipmap-hdpi","mipmap-xhdpi","mipmap-xxhdpi","mipmap-xxxhdpi"};
        int[] fg={108,162,216,324,432};       // adaptive foreground sizes
        int[] legacy={48,72,96,144,192};      // legacy square icon sizes
        for(int i=0;i<dirs.length;i++){
            write(foreground(fg[i]), res+"/"+dirs[i]+"/ic_launcher_foreground.png");
            write(fullIcon(legacy[i],false), res+"/"+dirs[i]+"/ic_launcher.png");
            write(fullIcon(legacy[i],true),  res+"/"+dirs[i]+"/ic_launcher_round.png");
        }
        write(fullIcon(512,false), store+"/play_store_512.png");
        write(fullIcon(1024,false), store+"/icon_1024.png");
    }
}
