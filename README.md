# dtc-doppler-illustrator

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)&nbsp;
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/af2e28a6c64745ac81552ab1dd3499ad)](https://www.codacy.com/app/dtczhl/dtc-doppler-illustrator?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=dtczhl/dtc-doppler-illustrator&amp;utm_campaign=Badge_Grade)  

Illustrating acoustic Doppler effect using Android phones

Two phones are required: one for emitting inaudible sounds and the other for calculating and displaying Doppler

## Screenshot

<img src="image/screenshot.jpg" width="120" alt="screenshot" />   

(Screenshot from Huawei Mate 20)

## Code Structure

It has mainly two components. One for transmitting signals, and the other one for analyzing received signals. Parts of codes are copied from my other two repositories [dtc-frequency-player](https://github.com/dtczhl/dtc-frequency-player) and [dtc-acoustic-spectrumer](https://github.com/dtczhl/dtc-acoustic-spectrumer). You could refer to these repositories for some details.

### Sender
The sender part is implemented in the `PlaySound.java` class, which emits sounds of a single frequency. The phone sends out sounds of 19 KHz. You can easily change to other frequencies if you like. See my repo [dtc-frequency-player](https://github.com/dtczhl/dtc-frequency-player)

### Receiver
The receiver part is implemented in the `AnalyzeFrequency.java` class. The acoustic signals go through the following processing

1)  receive sounds. See my repo [dtc-acoustic-spectrumer](https://github.com/dtczhl/dtc-acoustic-spectrumer)

2)  filtering. We apply a bandpass filter that only allows signals within \[18KHz, 21KHz\]. There is a very convenient Android app named *FIR Filter Designer* that can generate Java code for you. The higher order of filters causes greater computation overhead.

2)  undersampling and overlapping. Undersampling is applied to increase the frequency resolution and overlapping is used to increase temporal fidelity. We use 8X undersampling and 0.875 overlapping.

3)  window function. We apply Blackman-Harris window to smooth the spectrum. You can try other window functions as well. <https://en.wikipedia.org/wiki/Window_function>

4)  Fast-Fourier Transform (FFT). See my repo [dtc-acoustic-spectrumer](https://github.com/dtczhl/dtc-acoustic-spectrumer)

5)  calculate Doppler shift. Since we know that the sender's signal is 19 KHz, we only need to calculate the received frequency (with the strongest magnitude) minus 19 KHz.

6)  draw Doppler shift on screen. See my repo [dtc-acoustic-spectrumer](https://github.com/dtczhl/dtc-acoustic-spectrumer)

## More Details

This repo is part of my project. For more technical details please refer to my paper   
[Paper](https://www.huanlezhang.com/publication/conference/dopenc_mobicom_16/paper.pdf) DopEnc: Acoustic-based Encounter Profiling Using Smartphones, published in ACM Conference on Mobile Computing and Networking (MobiCom), 2016
Or [Paper](https://www.huanlezhang.com/publication/journal/dopenc_tmc_18/paper.pdf) An Acoustic-Based Encounter Profiling System, published in IEEE Transactions on Mobile Computing (TMC), 2018

Please also consider to cite my papers. Thank you.

## Phones Tested
*   Huawei Mate 20
*   Google Pixel 2
