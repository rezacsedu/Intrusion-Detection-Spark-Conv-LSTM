# A Scalable Deep Learning-based Intrusion Detection System using Conv-LSTM Network
Intrusion detection system with Apache Spark and deep learning

# Why and how to use this repository? 
This repository contains the implementation details and the code for our paper titled "A Scalable and Hybrid Deep Learning-based Intrusion Detection System using Convolutional-LSTM Network". This papers has been submitted to "Symmetry — Open Access Journal" (see http://www.mdpi.com/journal/symmetry). 

In network intrusion detection (IDS), anomaly-based approaches in particular suffer from accurate evaluation, comparison, and deployment which originates from the scarcity of adequate datasets. Many such datasets are internal and cannot be shared due to privacy issues, others are heavily anonymized and do not reflect current trends, or they lack certain statistical characteristics. These deficiencies are primarily the reasons why a perfect dataset is yet to exist. Thus, researchers must resort to datasets which they can obtain that are often suboptimal.

As network behaviours and patterns change and intrusions evolve, it has very much become necessary to move away from static and one-time datasets toward more dynamically generated datasets which not only reflect the traffic compositions and intrusions of that time, but are also modifiable, extensible, and reproducible.

As a proof-of-concept, we use the Intrusion detection evaluation dataset (ISCXIDS2012) to solve a classification problem, which accurately identifies anomalies. 

Nevertheless, to show the effectiveness of our proposed approach on both datasets, we implemented the first stage in Scala using Spark MLlib as the ML platform. The Conv-LSTM network, on the other hand, were implemented in Python using Keras. 

Experiments were performed on a computing cluster with 32 cores running 64-bit Ubuntu 14.04 OS. The software stack consisted of Apache Spark v2.3.0, Java (JDK) 1.8, Scala 2.11.8, and Keras. The Conv-LSTM network was trained on an Nvidia TitanX GPU with CUDA and cuDNN enabled to improve overall pipeline speed. 

## Spark MLlib-based classifers: 
The following classifiers have been implemented to solve both the classification problems in a 2 stage cascading style:
- Logistic Regression
- Decision Trees
- Random Forest
- Multilayer Perceptron (MLP).

Nevertheless, we implemnted Spark + H2O (aka. Sparkling Water) versions too. Take a look at the ArrhythmiaPredictionH2O.scala and URLReputationH2O.scala classes for the classification of the Cardiac Arrhythmia and indentifying suspicious URLs respectively. 

Make sure that Spark is properly configured. Also, you need to have Maven installed on Linux. If you prefer, Eclipse/IntelliJ IDEA, make sure that Maven plugin and Scala plugins are installed.  

If everything is properly configured, you can create a uber jar containing all the dependencies and execute the jar. Alternatively, you can execute each implementation as a stand-alone Scala project from your favourite IDE. 

## DeepLearning4j-based LSTM networks: 
The Long Short-term Memory (LSTM) network has been implemented to solve the classification problem. The following are prerequisites when working with DL4J:
- Java 1.8+ (64-bit only)
- Apache Maven for automated build and dependency manager
- IntelliJ IDEA or Eclipse IDE.

For more information on how to configure DeepLearning4j, please refer to https://deeplearning4j.org/. If everything is properly configured, you can create a uber jar containing all the dependencies and execute the jar. Alternatively, you can execute each implementation as a stand-alone Java project from your favourite IDE. 

## Citation request
If you reuse this implementation, please cite our paper: 

    @inproceedings{khan2018bigdata,
        title={A Scalable and Hybrid Deep Learning-based Intrusion Detection System using Convolutional-LSTM Network},
        author={M. A., Khan; Karim, Md. Rezaul; Y. Kim },
        booktitle={Symmetry — Open Access Journal},
        year={2019}
    }

## Contributing
For any questions, feel free to open an issue or contact at rezaul.karim@rwth-aachen.de
