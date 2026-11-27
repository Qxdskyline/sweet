const colors = ["#f6adc4", "#ffe7ee", "#e9c6e5", "#ffd8cc"];
const fullTurn = Math.PI * 2;
export const fullTurnDegrees = 360;

export function spinDestination(angle, winner, optionCount, random = Math.random) {
  // Segment zero starts centered under the pointer.
  const segmentWidth = fullTurnDegrees / optionCount;
  const landingOffset = (random() - 0.5) * segmentWidth * 0.8;
  const targetAngle = -winner * segmentWidth + landingOffset;
  const offset = (targetAngle - angle + 3 * fullTurnDegrees) % fullTurnDegrees;
  const turns = 4 + Math.floor(random() * 3);
  return angle + offset + turns * fullTurnDegrees;
}

export function drawWheel(canvas, options, pixelRatio) {
  if (!options.length) return;
  const pixels = Math.round(canvas.clientWidth * pixelRatio);
  if (!pixels) return;
  canvas.width = canvas.height = pixels;
  const context = canvas.getContext("2d");
  const radius = pixels / 2;
  const arc = fullTurn / options.length;
  for (const [index, option] of options.entries()) {
    const center = -Math.PI / 2 + index * arc;
    context.fillStyle = colors[index % colors.length];
    context.beginPath();
    context.moveTo(radius, radius);
    context.arc(radius, radius, radius, center - arc / 2, center + arc / 2);
    context.fill();
    context.strokeStyle = "#fff7fa";
    context.lineWidth = Math.max(1, pixelRatio);
    context.stroke();
    context.save();
    context.translate(radius, radius);
    context.rotate(center);
    context.fillStyle = "#643848";
    context.font = `600 ${Math.min(radius * .115, radius * arc * .32)}px sans-serif`;
    context.textAlign = "right";
    context.textBaseline = "middle";
    context.fillText(option, radius * .87, 0, radius * .65);
    context.restore();
  }
}
