#version 120

attribute vec3 Position;

varying vec2 texCoord;
varying vec2 oneTexel;

uniform vec2 InSize;
uniform vec2 OutSize;
uniform mat4 ProjMat;

void main() {
    vec4 outPos = ProjMat * vec4(Position.xy, 0.0, 1.0);
    gl_Position = vec4(outPos.xy, 0.2, 1.0);

    // texCoord en [0,1] sur l'ecran
    oneTexel = 1.0 / InSize;
    texCoord = Position.xy / OutSize;
}
