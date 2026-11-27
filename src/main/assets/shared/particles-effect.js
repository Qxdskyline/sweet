// 三个页面共用的星光背景。页面提供 #particles-canvas，引入模块后自动初始化。
// Canvas 的层级和点击穿透由 common.scss 控制。
import { readConfig } from "./config.js";

export const defaultParticles = {
  enabled: true, count: 30, foreground: 7, brightness: 70, speed: 70,
  size: 100, drift: 100, twinkle: 100, glow: 100, trail: 100
};
export const particleLimits = {
  count: [5, 50], foreground: [0, 15], brightness: [15, 100], speed: [20, 150],
  size: [50, 200], drift: [0, 200], twinkle: [0, 200], glow: [0, 200], trail: [0, 200]
};
let preferences = { ...defaultParticles };

export function particlePreferences(value) {
  const result = { ...defaultParticles };
  if (typeof value?.enabled === "boolean") result.enabled = value.enabled;
  for (const [key, [min, max]] of Object.entries(particleLimits)) {
    if (Number.isFinite(value?.[key])) result[key] = Math.round(Math.max(min, Math.min(max, value[key])));
  }
  return result;
}

const layers = [];
export function previewParticles(value) {
  preferences = particlePreferences(value);
  for (const refresh of layers) refresh();
}
const colors = ["255, 182, 193", "255, 192, 203", "255, 218, 224", "255, 240, 245", "255, 215, 180", "255, 160, 180", "255, 230, 250"];
const reducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)");
const background = document.querySelector("#particles-canvas");

function drawGlow(context, particle, x, y, size, alpha) {
  const radius = size * particle.glowScale;
  const glow = context.createRadialGradient(x, y, 0, x, y, radius);
  glow.addColorStop(0, `rgba(${particle.color}, ${alpha * 0.9})`);
  glow.addColorStop(0.4, `rgba(${particle.color}, ${alpha * 0.42})`);
  glow.addColorStop(1, `rgba(${particle.color}, 0)`);
  context.fillStyle = glow;
  context.fillRect(x - radius, y - radius, radius * 2, radius * 2);
}

// 尾迹来自粒子已经经过的位置，每两个基准帧采样一次。
function sampleTrail(particle, elapsed, alpha) {
  if (!particle.trail || !preferences.trail || reducedMotion.matches) {
    particle.trail?.splice(0);
    particle.sampleElapsed = 0;
    return;
  }
  particle.sampleElapsed += elapsed;
  if (particle.sampleElapsed >= 2) {
    particle.sampleElapsed %= 2;
    particle.trail.push({ x: particle.x, y: particle.y, rotation: particle.rotation, alpha });
  }
  const limit = Math.max(2, Math.round(particle.trailLength * preferences.trail / 100));
  if (particle.trail.length > limit) particle.trail.splice(0, particle.trail.length - limit);
}

function drawShape(context, shape, size) {
  context.beginPath();
  if (shape === "circle") {
    context.arc(0, 0, size, 0, Math.PI * 2);
  } else if (shape === "heart") {
    const half = size * 0.5;
    context.moveTo(0, half * 0.3);
    context.bezierCurveTo(-half, -half * 0.5, -half * 1.5, half * 0.3, 0, half * 1.8);
    context.bezierCurveTo(half * 1.5, half * 0.3, half, -half * 0.5, 0, half * 0.3);
  } else if (shape === "diamond") {
    context.moveTo(0, -size);
    context.lineTo(size * 0.7, 0);
    context.lineTo(0, size);
    context.lineTo(-size * 0.7, 0);
  } else {
    const tips = Number(shape.slice(-1));
    context.moveTo(0, -size);
    for (let index = 1; index < tips * 2; index++) {
      const angle = index * Math.PI / tips - Math.PI / 2;
      const radius = index % 2 ? size * 0.4 : size;
      context.lineTo(Math.cos(angle) * radius, Math.sin(angle) * radius);
    }
  }
  context.closePath();
  context.fill();
}

function drawTrail(context, particle, width, height, size) {
  const points = particle.trail;
  if (!preferences.trail || reducedMotion.matches || !points || points.length < 2) return;

  // 从最老的点画到最新的点，尺寸和透明度逐步增大。
  for (const [index, point] of points.entries()) {
    const progress = index / (points.length - 1);
    const alpha = Math.min(1, point.alpha * preferences.brightness / 70 * (0.05 + progress * 0.55));
    // 尾迹靠近星体时也保持纤细，主要靠透明度变化体现消散。
    const trailSize = size * (0.30 + progress * 0.25);
    context.save();
    context.translate(point.x * width, point.y * height);
    context.rotate(point.rotation);
    context.fillStyle = `rgba(${particle.color}, ${alpha})`;
    drawShape(context, particle.trailShape, trailSize);
    context.restore();
  }
}

function twinkleFactor(particle) {
  const wave = Math.sin(particle.phase);
  if (particle.twinkleMode === 0) return 0.5 + 0.5 * wave;
  if (particle.twinkleMode === 1) return 0.3 + 0.7 * Math.abs(Math.sin(2 * particle.phase));
  return 0.2 + 0.8 * (wave + 1) / 2;
}

function startParticles(canvas, count, palette) {
  const context = canvas.getContext("2d");
  if (!context) return;
  // x 和 y 是相对画布宽高的比例，窗口缩放时粒子保持相对位置。
  function createParticle() {
    const shapes = ["circle", "star5", "star6", "diamond", "heart"];
    const shape = shapes[Math.floor(Math.random() * shapes.length)];
    const sizes = shape === "circle" ? [1.5, 3] : shape === "heart" ? [3, 4] : [2, 3.5];
    const size = sizes[0] + Math.random() * sizes[1];
    const tier = Math.random();
    const [drift, rise, variation] = tier < 0.3 ? [0.6, 0.8, 0.6]
      : tier < 0.7 ? [0.4, 0.4, 0.4] : [0.2, 0.15, 0.25];
    return {
      x: Math.random(), y: Math.random(), shape, size,
      drift: (Math.random() - 0.5) * drift,
      rise: rise + Math.random() * variation,
      opacity: 0.3 + Math.random() * 0.5,
      phase: Math.random() * Math.PI * 2,
      twinkle: 0.008 + Math.random() * 0.02,
      twinkleMode: Math.floor(Math.random() * 3),
      rotation: Math.random() * Math.PI * 2,
      rotationSpeed: (Math.random() - 0.5) * 0.02,
      trail: Math.random() < 0.25 ? [] : null,
      trailShape: shape === "circle" || Math.random() < 0.5 ? "circle" : "star4",
      trailLength: 8 + Math.floor(Math.random() * 13),
      sampleElapsed: 0,
      glowScale: Math.random() < 0.3 && size > 3 ? 1.8 + Math.random() * 1.2 : 0,
      color: palette[Math.floor(Math.random() * palette.length)]
    };
  }
  const particles = [];
  let width;
  let height;
  let frame = null;
  let lastTime = null;

  function draw(elapsed) {
    context.clearRect(0, 0, width, height);
    if (!preferences.enabled) return;
    for (const [index, particle] of particles.entries()) {
      particle.x += particle.drift * elapsed * preferences.speed / 100 * preferences.drift / 100 / width;
      particle.y -= particle.rise * elapsed * preferences.speed / 100 / height;
      particle.rotation += particle.rotationSpeed * elapsed;
      particle.phase += particle.twinkle * elapsed * preferences.twinkle / 100;

      // 重新入场时重置路径，避免尾迹横跨整个屏幕。
      if (particle.y < -50 / height || particle.x < -50 / width || particle.x > 1 + 50 / width) {
        const replacement = createParticle();
        replacement.y = 1 + Math.random() * 100 / height;
        particles[index] = replacement;
        continue;
      }

      const x = particle.x * width;
      const y = particle.y * height;
      const size = particle.size * preferences.size / 100;
      const opacity = particle.opacity * twinkleFactor(particle);
      const alpha = Math.min(1, opacity * preferences.brightness / 70);
      sampleTrail(particle, elapsed, opacity);
      drawTrail(context, particle, width, height, size);
      if (preferences.glow && particle.glowScale) {
        drawGlow(context, particle, x, y, size, Math.min(1, alpha * preferences.glow / 100));
      }

      context.save();
      context.translate(x, y);
      context.rotate(particle.rotation);
      context.fillStyle = `rgba(${particle.color}, ${alpha})`;
      drawShape(context, particle.shape, size);
      if (particle.shape === "circle" && size > 3) {
        context.strokeStyle = `rgba(${particle.color}, ${alpha * 0.6})`;
        context.lineWidth = 1;
        context.beginPath();
        context.moveTo(-size * 1.5, 0);
        context.lineTo(size * 1.5, 0);
        context.moveTo(0, -size * 1.5);
        context.lineTo(0, size * 1.5);
        context.stroke();
      }
      context.restore();
    }
  }

  function resize() {
    width = Math.max(1, canvas.clientWidth);
    height = Math.max(1, canvas.clientHeight);
    // 高清屏使用更多绘制像素，倍率封顶为 2 以控制绘制开销。
    const ratio = Math.min(window.devicePixelRatio || 1, 2);
    canvas.width = Math.round(width * ratio);
    canvas.height = Math.round(height * ratio);
    context.setTransform(canvas.width / width, 0, 0, canvas.height / height, 0, 0);
    draw(0);
  }

  function animate(time) {
    // 用 60 FPS 的帧时长作为移动单位，限制卡顿后单帧的位移。
    const elapsed = lastTime === null ? 0 : Math.min((time - lastTime) / (1000 / 60), 3);
    lastTime = time;
    draw(elapsed);
    frame = requestAnimationFrame(animate);
  }

  function syncAnimation() {
    // 页面隐藏或系统启用减少动态效果时，暂停动画帧调度。
    if (document.hidden || reducedMotion.matches || !preferences.enabled || !particles.length) {
      if (frame !== null) cancelAnimationFrame(frame);
      frame = null;
      lastTime = null;
    } else if (frame === null) {
      frame = requestAnimationFrame(animate);
    }
  }

  window.addEventListener("resize", resize);
  document.addEventListener("visibilitychange", syncAnimation);
  reducedMotion.addEventListener?.("change", () => {
    draw(0);
    syncAnimation();
  });
  resize();
  layers.push(() => {
    const size = preferences[count];
    if (particles.length > size) particles.length = size;
    while (particles.length < size) particles.push(createParticle());
    draw(0);
    syncAnimation();
  });
}

if (background) {
  startParticles(background, "count", colors);

  // 少量颜色稍深的星光从卡片上方飘过，透明度由样式控制。
  const foreground = document.createElement("canvas");
  foreground.id = "particles-foreground";
  foreground.setAttribute("aria-hidden", "true");
  document.body.append(foreground);
  startParticles(foreground, "foreground", colors);
  previewParticles(defaultParticles);

  window.addEventListener("pageshow", () => {
    readConfig().then(config => previewParticles(config.particles)).catch(() => {});
  });
}
