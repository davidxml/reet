# Documentation
# Fleet Tracking Implementation Documentation

## Project Context
This section details my contribution to the LAG CSC 210 Concurrent Programming project. The primary objective was to design a robust and thread safe vehicle tracking system capable of handling concurrent read and write operations without any data corruption.

## Research and Baseline Analysis
Before writing any code, I conducted a thorough review of existing open source implementations of concurrent vehicle trackers. Studying these public repositories allowed me to understand the baseline requirements for thread safety in Java and identify common architectural pitfalls, particularly representation exposure. Armed with this foundational understanding, I proceeded to engineer a custom implementation strictly tailored to our course requirements.

## Theoretical Foundation
The architecture heavily relies on the principles outlined in Java Concurrency in Practice, specifically Chapter 4, Section 4.2.2. This exact textbook chapter details the Java monitor pattern, which serves as the core synchronization strategy for my entire implementation.

## Implementation Architecture
The solution is divided into three primary components. First, the MutablePoint class serves as a simple data container for horizontal and vertical coordinates. It intentionally lacks built in synchronization, relying entirely on the parent tracker for its safety.

Second, the MonitorVehicleTracker class acts as the central coordinator. It encapsulates a shared map of vehicle locations behind a single intrinsic lock. To guarantee absolute thread safety, this class utilizes defensive copying. It creates deep copies of the location data at every boundary, whether during initialization, reading, or writing. This strict isolation prevents outside threads from secretly modifying the live internal state.

## Concurrent Stress Testing
To validate the robustness of the system, I developed a dedicated test harness. This test suite utilizes an executor service thread pool to simulate a realistic and high volume environment. It spawns continuous writer threads that push rapid coordinate updates while multiple reader threads simultaneously pull fleet snapshots. Running this stress test practically proves that the intrinsic locking and copy on read mechanisms successfully prevent torn reads and maintain strict data consistency under heavy concurrent load.