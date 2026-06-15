package com.tertiaryinfotech.fractalapp.gl

/**
 * GLSL ES 2.0 shaders for the escape-time fractal renderer — a port of the iOS
 * `Fractals.metal` shader. ES 2.0 has no `switch` and requires a constant loop
 * bound, so families/palettes use if/else and the loop runs to a fixed maximum
 * with an early `break` once `uMaxIter` is reached.
 */
object FractalShaders {

    const val VERTEX = """
        attribute vec2 aPos;
        varying vec2 vUv;
        void main() {
            vUv = aPos;                 // fullscreen triangle in clip space, reused as -1..1 uv
            gl_Position = vec4(aPos, 0.0, 1.0);
        }
    """

    // 700 == FractalParameters.MAX_SHADER_ITERATIONS
    const val FRAGMENT = """
        precision highp float;
        varying vec2 vUv;

        uniform vec2 uCenter;
        uniform vec2 uSeed;
        uniform float uScale;
        uniform float uAspect;
        uniform int uMaxIter;
        uniform int uType;
        uniform int uPalette;
        uniform float uColorShift;
        uniform float uColorDensity;
        uniform float uInterior;

        vec3 cosPalette(float t, vec3 a, vec3 b, vec3 c, vec3 d) {
            return a + b * cos(6.28318530718 * (c * t + d));
        }

        vec3 paletteColor(int pal, float t) {
            t = fract(t);
            if (pal == 0) return cosPalette(t, vec3(0.15,0.30,0.35), vec3(0.45,0.45,0.40), vec3(1.0), vec3(0.0,0.20,0.45));
            else if (pal == 1) return cosPalette(t, vec3(0.50,0.22,0.10), vec3(0.55,0.45,0.30), vec3(1.0), vec3(0.0,0.10,0.20));
            else if (pal == 2) return cosPalette(t, vec3(0.10,0.30,0.50), vec3(0.30,0.40,0.45), vec3(1.0), vec3(0.55,0.65,0.80));
            else if (pal == 3) return cosPalette(t, vec3(0.50,0.50,0.50), vec3(0.55,0.45,0.55), vec3(1.0), vec3(0.0,0.33,0.66));
            else if (pal == 4) return cosPalette(t, vec3(0.60,0.35,0.30), vec3(0.45,0.35,0.25), vec3(1.0,1.0,0.90), vec3(0.0,0.15,0.30));
            else if (pal == 5) return cosPalette(t, vec3(0.28,0.42,0.30), vec3(0.35,0.45,0.30), vec3(1.0), vec3(0.30,0.10,0.65));
            else if (pal == 6) return cosPalette(t, vec3(0.60,0.50,0.65), vec3(0.40,0.45,0.35), vec3(1.0), vec3(0.10,0.40,0.70));
            return vec3(t);
        }

        vec2 cmul(vec2 a, vec2 b) {
            return vec2(a.x * b.x - a.y * b.y, a.x * b.y + a.y * b.x);
        }

        vec2 iterate(int type, vec2 z, vec2 zPrev, vec2 c) {
            if (type == 2) {                            // Burning Ship
                vec2 a = vec2(abs(z.x), abs(z.y));
                return cmul(a, a) + c;
            } else if (type == 3) {                     // Tricorn
                vec2 zc = vec2(z.x, -z.y);
                return cmul(zc, zc) + c;
            } else if (type == 4) {                     // Phoenix
                return cmul(z, z) + vec2(c.x, 0.0) + vec2(c.y * zPrev.x, c.y * zPrev.y);
            } else if (type == 5) {                     // Celtic
                vec2 z2 = cmul(z, z);
                return vec2(abs(z2.x), z2.y) + c;
            }
            return cmul(z, z) + c;                       // Mandelbrot / Julia
        }

        void main() {
            vec2 p = vec2(vUv.x * uAspect, vUv.y) * uScale + uCenter;
            vec2 z; vec2 c; vec2 zPrev;
            bool isJulia = (uType == 1) || (uType == 4);
            if (isJulia) { z = p; c = uSeed; } else { z = vec2(0.0); c = p; }
            zPrev = vec2(0.0);

            float escape = 64.0;
            int iCount = 0;
            bool escaped = false;
            for (int i = 0; i < 700; i++) {
                if (i >= uMaxIter) break;
                vec2 zNext = iterate(uType, z, zPrev, c);
                zPrev = z;
                z = zNext;
                iCount = i + 1;
                if (dot(z, z) > escape * escape) { escaped = true; break; }
            }

            vec3 color;
            if (!escaped) {
                color = vec3(uInterior * 0.18, uInterior * 0.16, uInterior * 0.22);
            } else {
                float mag = max(length(z), 1.0001);
                float nu = log(log(mag) / log(escape)) / log(2.0);
                float smoothI = float(iCount) + 1.0 - nu;
                float t = smoothI * 0.018 * uColorDensity + uColorShift;
                color = paletteColor(uPalette, t);
                color *= 0.4 + 0.6 * smoothstep(0.0, 1.0, float(iCount) / float(uMaxIter) * 3.0);
            }
            color = color / (color + 0.85);
            color = pow(color, vec3(0.85));
            gl_FragColor = vec4(color, 1.0);
        }
    """
}
