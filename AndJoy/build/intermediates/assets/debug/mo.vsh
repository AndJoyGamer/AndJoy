attribute vec3 aPosition;
uniform mat4 uPVMMatrix;
varying vec2 vPosition;

void main()
{
	gl_Position = uPVMMatrix * vec4(aPosition , 1);
	vPosition = aPosition.xy / 10.0;
}