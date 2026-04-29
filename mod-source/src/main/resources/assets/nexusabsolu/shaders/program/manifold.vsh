#version 120

attribute vec4 Position;

uniform mat4 ProjMat;
uniform vec2 OutSize;
uniform vec2 InSize;

varying vec2 texCoord;
varying vec2 oneTexel;

void main(){
    // ProjMat est rempli automatiquement par MC chaque frame
    vec4 outPos = ProjMat * vec4(Position.xy, 0.0, 1.0);
    gl_Position = vec4(outPos.xy, 0.2, 1.0);

    // Pattern vanilla MC : Position est en pixels (0..OutSize), donc on
    // divise pour avoir des coordonnees [0..1] dans texCoord
    oneTexel = 1.0 / InSize;
    texCoord = Position.xy / OutSize;
}
