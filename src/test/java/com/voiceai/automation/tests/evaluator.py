#!/usr/bin/env python3
# evaluator.py - evaluate hallucination & context drift for mock RAG system

from server import qa_handler
import re

# -------------------------------------------------------------
# QUESTIONS
# -------------------------------------------------------------
QUESTIONS = [
    ("c-001", "What is HighLevel?"),
    ("c-002", "Who can use HighLevel?"),
    ("c-003", "Does HighLevel provide analytics?")
]

# GOLD SUPPORT (correct document or None)
GOLD = {
    0: "D1",   # Q1 → D1
    1: "D3",   # Q2 → D3
    2: None    # Q3 → No supporting doc
}

_word_re = re.compile(r"\w+")


def _tokens(text):
    return [t.lower() for t in _word_re.findall(text)]


# -------------------------------------------------------------
# HALLUCINATION RULE:
# hallucinated if >3 answer tokens NOT found in contexts
# -------------------------------------------------------------
def is_hallucinated(answer, contexts):
    ans_tokens = _tokens(answer)
    concat = " ".join([c["text"] for c in contexts])
    ctx_tokens = set(_tokens(concat))

    missing = [t for t in ans_tokens if t not in ctx_tokens]
    return len(missing) > 3, missing


# -------------------------------------------------------------
# CONTEXT ACCURACY RULE:
# correct if any returned context matches gold support
# -------------------------------------------------------------
def context_accuracy(contexts, gold_doc):
    if gold_doc is None:
        return 0 if contexts else 1
    for c in contexts:
        if c["doc_id"] == gold_doc:
            return 1
    return 0


# -------------------------------------------------------------
# MAIN EXECUTION
# -------------------------------------------------------------
def main():
    total = len(QUESTIONS)
    hallucinated_total = 0
    context_correct_total = 0

    print("Running evaluator...\n")

    for idx, (cid, q) in enumerate(QUESTIONS):
        resp = qa_handler({"conversation_id": cid, "question": q})

        hall, missing = is_hallucinated(resp["answer"], resp["contexts"])
        ctx_acc = context_accuracy(resp["contexts"], GOLD[idx])

        hallucinated_total += hall
        context_correct_total += ctx_acc

        print(f"Question: {q}")
        print(f"  Contexts: {[c['doc_id'] for c in resp['contexts']]}")
        print(f"  Answer: {resp['answer']}")
        print(f"  Hallucinated: {hall}")
        print(f"  Context Accurate: {bool(ctx_acc)}\n")

    print("=== METRICS ===")
    print(f"Hallucination %: {(hallucinated_total / total) * 100:.2f}")
    print(f"Context Retrieval Accuracy %: {(context_correct_total / total) * 100:.2f}")


if __name__ == "__main__":
    main()
