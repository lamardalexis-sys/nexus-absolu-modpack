#version 120

uniform sampler2D DiffuseSampler;     // l'image du jeu (input)
uniform float Time;                    // temps en secondes (injecte par Java via reflection)
uniform vec2 InSize;                   // resolution ecran
uniform float Intensity;               // 0.0..1.0 — fade in/out de l'effet
uniform float Phase;                   // 0=active 1=negative

varying vec2 texCoord;
varying vec2 oneTexel;

#define PI  3.14159265359
#define TAU 6.28318530718

// Performance MC : on reduit les iterations vs HTML original (140 -> 60)
// pour que le jeu reste jouable. Le rendu sera moins detaille mais OK.
#define RAYMARCH_STEPS 60
#define MANDELBULB_ITER 7

mat2 rot(float a){ float c=cos(a),s=sin(a); return mat2(c,-s,s,c); }

vec3 pal(float t){
    vec3 a = vec3(0.5);
    vec3 b = vec3(0.5);
    vec3 c = vec3(1.0, 1.0, 1.0);
    vec3 d = vec3(0.00, 0.33, 0.67);
    return a + b * cos(TAU * (c*t + d));
}

vec3 pal2(float t){
    vec3 a = vec3(0.5, 0.5, 0.5);
    vec3 b = vec3(0.5, 0.5, 0.5);
    vec3 c = vec3(2.0, 1.0, 0.0);
    vec3 d = vec3(0.50, 0.20, 0.25);
    return a + b * cos(TAU * (c*t + d));
}

// MANDELBULB power 7 (reduit de 8 pour perf)
float mandelbulb(vec3 p, out float orbit){
    vec3  z   = p;
    float dr  = 1.0;
    float r   = 0.0;
    float power = 7.0 + sin(Time * 0.1) * 1.5;
    float bail = 2.0;
    orbit = 1e9;

    for(int i = 0; i < MANDELBULB_ITER; i++){
        r = length(z);
        if(r > bail) break;

        float theta = acos(clamp(z.z / r, -1.0, 1.0));
        float phi   = atan(z.y, z.x);
        dr = pow(r, power - 1.0) * power * dr + 1.0;

        float zr = pow(r, power);
        theta *= power;
        phi   *= power;

        z = zr * vec3(sin(theta) * cos(phi),
                      sin(phi)   * sin(theta),
                      cos(theta));
        z += p;

        orbit = min(orbit, dot(z, z));
    }
    return 0.5 * log(r) * r / dr;
}

float scene(vec3 p, out float orbit){
    p.xz *= rot(Time * 0.06);
    p.yz *= rot(Time * 0.04);
    return mandelbulb(p, orbit);
}

vec3 calcNormal(vec3 p){
    vec2 e = vec2(0.001, -0.001);
    float ob;
    return normalize(
        e.xyy * scene(p + e.xyy, ob) +
        e.yyx * scene(p + e.yyx, ob) +
        e.yxy * scene(p + e.yxy, ob) +
        e.xxx * scene(p + e.xxx, ob)
    );
}

void main(){
    // image du jeu en arriere-plan
    vec4 gameColor = texture2D(DiffuseSampler, texCoord);

    // Si Intensity = 0, on retourne juste le jeu (skip raymarching)
    if(Intensity < 0.01){
        gl_FragColor = gameColor;
        return;
    }

    // UV centre sur l'ecran [-1.5, 1.5] avec aspect ratio
    vec2 uv = (texCoord - 0.5) * 2.0;
    uv.x *= InSize.x / InSize.y;

    // Camera fixe a -2.5 avec leger drift
    float camD = -2.5 + sin(Time * 0.13) * 0.3;
    vec3 ro = vec3(sin(Time * 0.08) * 0.25,
                   cos(Time * 0.10) * 0.25,
                   camD);
    vec3 rd = normalize(vec3(uv, 1.4));
    rd.xy *= rot(Time * 0.03);

    // raymarching
    float t = 0.0;
    float orbit = 1e9;
    float steps = 0.0;
    bool hit = false;
    float maxT = 20.0;

    for(int i = 0; i < RAYMARCH_STEPS; i++){
        vec3 p = ro + rd * t;
        float ob;
        float d = scene(p, ob);
        steps = float(i);
        float eps = max(0.001, 0.001 * t);
        if(d < eps){ orbit = ob; hit = true; break; }
        if(t > maxT) break;
        t += d * 0.7;
    }

    vec3 fractalCol = vec3(0.0);
    if(hit){
        vec3 p = ro + rd * t;
        vec3 n = calcNormal(p);

        // Lighting simple
        vec3 lDir1 = normalize(vec3(0.6, 0.7, -0.4));
        float diff1 = max(dot(n, lDir1), 0.0);

        // Fresnel rim (= iridescence DMT)
        float fres = pow(1.0 - max(dot(n, -rd), 0.0), 3.0);

        // Couleurs auto-illuminees via orbit trap
        float trap = sqrt(orbit);
        vec3 emit = pal(trap * 0.4 + Time * 0.08);
        vec3 amb = pal2(length(p) * 0.2 + Time * 0.05) * 0.5;

        fractalCol  = emit * 0.7;
        fractalCol += amb;
        fractalCol += pal(Time * 0.1 + 0.2) * diff1 * 0.6;
        fractalCol += pal(trap * 0.3 + Time * 0.2 + 0.5) * fres * 1.2;
        fractalCol += pal(Time * 0.15) * (steps / float(RAYMARCH_STEPS)) * 0.25;
        fractalCol *= mix(1.0, 0.4, clamp(t / maxT, 0.0, 1.0));
    } else {
        // Background : tunnel colore
        float r = length(uv);
        float a = atan(uv.y, uv.x);
        fractalCol  = pal(r * 1.2 + Time * 0.2 + a * 0.05) * 0.3;
    }

    // Saturation boost
    float lum = dot(fractalCol, vec3(0.299, 0.587, 0.114));
    fractalCol = mix(vec3(lum), fractalCol, 1.35);

    // Tone mapping + gamma
    fractalCol = fractalCol / (1.0 + fractalCol);
    fractalCol = pow(fractalCol, vec3(0.85));

    // Vignette
    fractalCol *= 1.0 - 0.3 * dot(uv, uv);

    // PHASE 2 (negatif) : invert fractal
    if(Phase > 0.5){
        fractalCol = vec3(1.0) - fractalCol;
    }

    // Blend final : melange jeu + fractale selon Intensity
    // Mode "screen" (1 - (1-a)(1-b)) pour faire briller les couleurs
    vec3 inv = vec3(1.0) - gameColor.rgb;
    vec3 invF = vec3(1.0) - fractalCol;
    vec3 screened = vec3(1.0) - inv * invF;

    vec3 final = mix(gameColor.rgb, screened, Intensity * 0.85);

    gl_FragColor = vec4(final, 1.0);
}
