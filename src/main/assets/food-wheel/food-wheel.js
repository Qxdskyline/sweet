import { startMainPage } from "../shared/page.js";
import { drawWheel, fullTurnDegrees, spinDestination } from "./wheel.js";

const canvas = document.querySelector("#wheel-canvas");
const pointer = document.querySelector(".pointer");
const button = document.querySelector("#spin");
const spinLabel = document.querySelector("#spin-label");
const result = document.querySelector("#result");
let options = [];
let angle = 0;
let animation;

function draw() {
  drawWheel(canvas, options, window.devicePixelRatio);
}

function finish(winner, destination) {
  angle = destination % fullTurnDegrees;
  canvas.style.transform = `rotate(${angle}deg)`;
  animation = undefined;
  pointer.classList.remove("spinning");
  result.textContent = `今天吃 ${options[winner]}`;
  spinLabel.textContent = "再转一次";
  button.disabled = false;
}

button.addEventListener("click", () => {
  const winner = Math.floor(Math.random() * options.length);
  const destination = spinDestination(angle, winner, options.length);
  if (window.matchMedia("(prefers-reduced-motion: reduce)").matches || !canvas.animate) {
    finish(winner, destination);
    return;
  }
  pointer.classList.add("spinning");
  result.textContent = "转盘转动中";
  const distance = destination - angle;
  animation = canvas.animate([
    { transform: `rotate(${angle}deg)`, easing: "cubic-bezier(.55,0,.8,.65)" },
    { transform: `rotate(${angle + distance * .13}deg)`, offset: .18,
      easing: "cubic-bezier(.12,.32,.18,1)" },
    { transform: `rotate(${destination}deg)` }
  ], { duration: 2650 + distance * .32, fill: "forwards" });
  button.disabled = true;
  const spin = animation;
  spin.onfinish = () => {
    finish(winner, destination);
    spin.cancel();
  };
});

window.addEventListener("resize", draw);
startMainPage(({ foodOptions }) => {
  const sameMenu = options.length === foodOptions.length
    && options.every((option, index) => option === foodOptions[index]);
  if (sameMenu) return;

  if (animation) {
    animation.cancel();
    animation = undefined;
    pointer.classList.remove("spinning");
  }
  options = foodOptions;
  angle = 0;
  canvas.style.transform = "";
  result.textContent = "";
  spinLabel.textContent = "开始转";
  button.disabled = options.length < 2;
  draw();
});
