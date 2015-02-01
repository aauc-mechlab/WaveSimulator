WaveSimulator
================
In this work, a framework is presented that makes it possible to reproduce the challenging operational scenario of controlling offshore cranes via a laboratory setup. This framework can be used for testing different control methods and for training purposes. The system consists of an industrial robot, the Kuka KR 6 R900 SIXX (KR AGILUS) manipulator and a motion platform with three degrees of freedom. This work focuses on the system integration. The motion platform is used to simulate the wave effects, while the robotic arm is controlled by the user with a joystick. The wave contribution is monitored by means of an accelerometer mounted on the platform and it is used as a negative input to the manipulator’s control algorithm so that active heave compensation methods can be achieved. Concerning the system architecture, the presented framework is built on open-source software and hardware. The control software is realised by applying strict multi-threading criteria to meet demanding real-time requirements. Related simulations and experimental results are carried out to validate the efficiency of the proposed framework. In particular, it can be certified that this approach allows for an effective risk reduction from both an individual as well as an overall evaluation of the potential harm.

References
================
F. Sanfilippo, L. I. Hatledal, H. Zhang, W. Rekdalsbakken and K. Y. Pettersen. A Wave Simulator and Active Heave Compensation Framework for Demanding Offshore Crane Operations. In Proceeding of the 2015 IEEE 28th Canadian Conference on Electrical and Computer Engineering (CCECE2015), Halifax, Nova Scotia, Canada.

Acknowledgements
================
The authors gratefully acknowledge the contribution of Ottar Laurits Osen in the design of the platform. The contribution of the students Håkon Østrem, Håkon Eikrem and Bjarne Humlen in the implementation of this work is also highly appreciated by the authors. This work was partly supported by the Research Council of Norway through the Centres of Excellence funding scheme, project No. 223254 - AMOS.

Demo Video
================
http://youtu.be/EJXgOPpp4a4
