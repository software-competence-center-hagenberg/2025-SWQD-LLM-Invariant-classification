
# Automating Invariant Filtering: Leveraging LLMs to Streamline Test Oracle Generation

This is the online appendix for a paper submitted to the [Software Quality Days](https://www.software-quality-days.com/vortragende/call-for-practical-papers) 2025.

## Abstract

Automated generation of test oracles is a critical area of research in software quality assurance. 
One effective technique is the detection of invariants by analyzing dynamic execution data. 
Though a common challenge of these approaches is the detection of false-positive invariants. 
This paper investigates the potential of Large Language Models (LLMs) to assist in filtering these dynamically detected invariants, aiming to reduce the manual effort involved in discarding incorrect invariants. 
We conducted experiments using various GPT models from OpenAI, leveraging a dataset of invariants detected from the dynamic execution of a REST API. 
By employing a Zero-shot Chain-of-Thought Prompting methodology, we guided the LLMs to articulate their reasoning behind their decisions. 
Our findings indicate that classification performance significantly improves with detailed instructions and strategic prompt design (the best model achieving on average 80.7% accuracy), with some performance differences between different invariant types.


## Contents

1. Directory [InvariantClassifier](./InvariantClassifier) contains the Java Gradle project for executing the experiments and analyzing the data
   1. Sub-Directory [agora_data](./InvariantClassifier/agora_data) contains the data from the [original study](https://doi.org/10.1145/3597926.3598114) used for the experiments (the execution data is not included due to size limitations)
   2. Sub-Directory [results](./InvariantClassifier/results) contains the result data for our first experiment setup
   3. Sub-Directory [results_improved_prompt](./InvariantClassifier/results_improved_prompt) contains the result data for our second experiment setup
      - The results are grouped by the API operation and LLM.
      - The results for each invariant contain the prompt generated for interacting with the LLM, the reasoning response in markdown, and a JSON with the data used for further processing.
2. Directory [plots](./plots) contains a host of plots for the experiment results (some of which are in the full paper, other had to be cut because of space limitations)


## Results


### Setup 1 (Initial Setup: Simple System Message and Static Prompts)

**GPT-3.5:**

|       |                  | GPT Verdict     |                  |
|-------|------------------|-----------------|------------------|
|       |                  | _true-positive_ | _false-positive_ |
| Label | _true-positive_  | 195             | 674              |
|       | _false-positive_ | 96              | 766              |

- **Accuracy:** 0.5551704217215483
- **F1-Score:** 0.33620689655172414
- **Precision:** 0.6701030927835051
- **Recall:** 0.2243958573072497
- **TPR:** 0.2243958573072497
- **FPR:** 0.11136890951276102

**GPT-4o-mini:**

|       |                  | GPT Verdict     |                  |
|-------|------------------|-----------------|------------------|
|       |                  | _true-positive_ | _false-positive_ |
| Label | _true-positive_  | 69              | 800              |
|       | _false-positive_ | 4               | 858              |

- **Accuracy:** 0.5355285961871751
- **F1-Score:** 0.1464968152866242
- **Precision:** 0.9452054794520548
- **Recall:** 0.07940161104718067
- **TPR:** 0.07940161104718067
- **FPR:** 0.004640371229698376


### Setup 2 (Enhanced Setup: Improved System Message and Data Examples)

**GPT-3.5:**

|       |                  | GPT Verdict     |                  |
|-------|------------------|-----------------|------------------|
|       |                  | _true-positive_ | _false-positive_ |
| Label | _true-positive_  | 783             | 86               |
|       | _false-positive_ | 684             | 178              |

- **Accuracy:** 0.5551704217215483
- **F1-Score:** 0.6703767123287672
- **Precision:** 0.5337423312883436
- **Recall:** 0.9010356731875719
- **TPR:** 0.9010356731875719
- **FPR:** 0.7935034802784223

**GPT-4o-mini:**

|       |                  | GPT Verdict     |                  |
|-------|------------------|-----------------|------------------|
|       |                  | _true-positive_ | _false-positive_ |
| Label | _true-positive_  | 581             | 288              |
|       | _false-positive_ | 129             | 733              |

- **Accuracy:** 0.7590987868284229
- **F1-Score:** 0.7359088030398987
- **Precision:** 0.8183098591549296
- **Recall:** 0.6685845799769851
- **TPR:** 0.6685845799769851
- **FPR:** 0.14965197215777262

**GPT-4o:**

|       |                  | GPT Verdict     |                  |
|-------|------------------|-----------------|------------------|
|       |                  | _true-positive_ | _false-positive_ |
| Label | _true-positive_  | 764             | 105              |
|       | _false-positive_ | 229             | 633              |

- **Accuracy:** 0.8070479491623339
- **F1-Score:** 0.8206229860365198
- **Precision:** 0.7693856998992951
- **Recall:** 0.8791714614499425
- **TPR:** 0.8791714614499425
- **FPR:** 0.265661252900232
