# Fractal Paint

`sbt run` should launch the window

## How this works

You mush the buttons and it produces beautiful pictures. Don't forget to press Space from time to time.

## How to draw like this
take a regular polygon and pick a random vertex, then travel half-way towards it (starting point will be the top-most point in our case). Draw a dot in your end-position. Pick another random vertex, travel half way to it. Repeat X times. (Better explanation in [this YouTube video](https://youtu.be/kbKtFN71Lfs))

This app lets you control several parameters: do you travel half-way / quarter-way / one-and-a-half-way towards the next random vertex? Can you repeatedly choose the same vertex? How many dots do you want to draw? and a few more, see table below.

## Keys

keys | function
--- | ---
Up/Down | increase/decrease distance a dot will travel from one point to the next vertex of the master polygon
Left/Right | rotate CCW/CW
I/O | zoom In / Out
A/Z | add/remove points to the master polygon
+/- | more/fewer dots will be painted each redraw
Space | toggle redrawing of the whole canvas on each refresh
C | add an additional point in the centre of the polygon
R | restrict choice of next point (dot will not attempt to go towards the same point twice in a row)
T | tether the hue change to every re-draw of the image
H | change the hue of the dots' colour
Backspace | play back all the operations since last playback buffer reset
0 (zero) | reset playback buffer (start recording from frame 0)
N | new canvas, start from scratch
Q | quit

## Examples

![Example 1](readme/colourful-flowers.png)
![Example 2](readme/triangle.png)
![Example 3](readme/embroidery.png)
![Example 4](readme/biohazard-like.png)
![Example 5](readme/electric.png)
![Example 6](readme/flowers.png)
![Example 7](readme/sharp.png)
![Animated example](readme/animated.gif)


[![YouTube example](https://img.youtube.com/vi/vfpxNSX8G9E/2.jpg)](https://www.youtube.com/watch?v=vfpxNSX8G9E) Click the image to watch an example video on YouTube


This project was inspired by this tweet:
https://twitter.com/CentrlPotential/status/1250172108811927552


App was created by Peter Perhac on Friday evening (15 May 2020) and perfected little by little over the next few days. This repository was created on Monday 19 May 2020.
