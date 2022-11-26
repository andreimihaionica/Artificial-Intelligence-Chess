import os
import torch
import numpy as np
from torch import nn
import torch.nn.functional as F
from torch.utils.data import Dataset, DataLoader, IterableDataset, random_split
import pytorch_lightning as pl
from random import randrange

from main import Evaluations

LABEL_COUNT = 37164639

class EvaluationDataset(IterableDataset):
    def __init__(self, count):
        self.count = count

    def __iter__(self):
        return self

    def __next__(self):
        idx = randrange(self.count)
        return self[idx]

    def __len__(self):
        return self.count

    def __getitem__(self, idx):
        eval = Evaluations.get(Evaluations.id == idx + 1)
        bin = np.frombuffer(eval.binary, dtype=np.uint8)
        bin = np.unpackbits(bin, axis=0).astype(np.single)
        eval.eval = max(eval.eval, -15)
        eval.eval = min(eval.eval, 15)
        ev = np.array([eval.eval]).astype(np.single)
        return {'binary': bin, 'eval': ev}


def get_dataset():
    return EvaluationDataset(count=LABEL_COUNT)
