import time
import torch
import torch.nn.functional as F
import pytorch_lightning as pl
from torch import nn
from collections import OrderedDict
from torch.utils.data import DataLoader
from EvaluationDataset import EvaluationDataset

LABEL_COUNT = 37164639


class EvaluationModel(pl.LightningModule):
    def __init__(self, learning_rate=1e-3, batch_size=1024, layer_count=10):
        super().__init__()
        self.batch_size = batch_size
        self.learning_rate = learning_rate
        layers = []
        for i in range(layer_count - 1):
            layers.append((f"linear-{i}", nn.Linear(808, 808)))
            layers.append((f"relu-{i}", nn.ReLU()))
        layers.append((f"linear-{layer_count - 1}", nn.Linear(808, 1)))
        self.seq = nn.Sequential(OrderedDict(layers))

    def forward(self, x):
        return self.seq(x)

    def training_step(self, batch, batch_idx):
        x, y = batch['binary'], batch['eval']
        y_hat = self(x)
        loss = F.l1_loss(y_hat, y)
        self.log("train_loss", loss)
        return loss

    def configure_optimizers(self):
        return torch.optim.Adam(self.parameters(), lr=self.learning_rate)

    def train_dataloader(self):
        dataset = EvaluationDataset(count=LABEL_COUNT)
        return DataLoader(dataset, batch_size=self.batch_size, num_workers=2, pin_memory=True)

    def get_model(self):
        configs = [
            {"layer_count": 4, "batch_size": 512},
            #  {"layer_count": 6, "batch_size": 1024},
        ]
        for config in configs:
            version_name = f'{int(time.time())}-batch_size-{config["batch_size"]}-layer_count-{config["layer_count"]}'
            logger = pl.loggers.TensorBoardLogger("lightning_logs", name="chessml", version=version_name)
            trainer = pl.Trainer(gpus=1, precision=16, max_epochs=1, auto_lr_find=True, logger=logger)
            model = EvaluationModel(layer_count=config["layer_count"], batch_size=config["batch_size"],
                                    learning_rate=1e-3)
            # trainer.tune(model)
            # lr_finder = trainer.tuner.lr_find(model, min_lr=1e-6, max_lr=1e-3, num_training=25)
            # fig = lr_finder.plot(suggest=True)
            # fig.show()
            trainer.fit(model)
            break
        return model
