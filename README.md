# dtc-doppler-illustrator

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## I am remaking this repo. You can see the branch `original` for the old repo.  

Illustrating acoustic Doppler effect using Android phones

Two phones are required: one for emitting inaudible sounds and the other for calculating and displaying Doppler

## Code Structure

It has mainly two components

1. The sender part is implemented in the `PlaySound.java` class, which emits sounds of a single frequency. The phone sends out sounds of 19 KHz. You can easily change to other frequencies if you like. For details please refer to my repo [dtc-frequency-player](https://github.com/dtczhl/dtc-frequency-player)

2. The receiver part is implemented in the `AnalyzeFrequency.java` class, which does the following tasks: (1) receiving sound, (2) filtering sound signals, (3) calculating the frequency and (4) the frequency shift, (5) drawing the Doppler shift on screen. I will explain (2) filtering sounds here. For other parts of code, you can refer to my repo XXX.
