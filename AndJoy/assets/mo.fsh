precision mediump float;

varying vec2 vPosition;

uniform float time;

const float color_intensity = 0.45;
const float Pi = 3.14159;

void main()
{
  vec2 p=(2.*vPosition);
  for(int i=1;i<8;i++)
  {
    vec2 newp=p;
    newp.x+=0.5/float(i)*sin(float(i)*Pi*p.y+time*.1)+0.1;
    newp.y+=0.5/float(i)*cos(float(i)*Pi*p.x+time*.1)-0.1;
    p=newp;
  }
  vec3 col=vec3(sin(p.x+p.y)+.5,sin(p.x+p.y+6.)*.5+.5,sin(p.x+p.y+12.)*.2+.5);
  vec4 color = vec4(col*col, 1.0) ;
  float fLimit = 0.2;
  if(color.r < 0.1 && color.g < fLimit && color.b < fLimit)
  	discard;
  gl_FragColor=color;
}