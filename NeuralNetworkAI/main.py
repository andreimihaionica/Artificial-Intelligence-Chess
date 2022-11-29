from peewee import *
import base64
from IPython.display import display, SVG
from random import randrange

db = SqliteDatabase('2021-07-31-lichess-evaluations-37MM.db')


class Evaluations(Model):
    id = IntegerField()
    fen = TextField()
    binary = BlobField()
    eval = FloatField()

    class Meta:
        database = db

    def binary_base64(self):
        return base64.b64encode(self.binary)


db.connect()
LABEL_COUNT = 37164639
print(LABEL_COUNT)
eval = Evaluations.get(Evaluations.id == 1)
print(eval.binary_base64())

SVG_BASE_URL = "https://us-central1-spearsx.cloudfunctions.net/chesspic-fen-image/"


def svg_url(fen):
    fen_board = fen.split()[0]
    return SVG_BASE_URL + fen_board


def show_index(idx):
    eval = Evaluations.select().where(Evaluations.id == idx + 1).get()
    batch = dataset[idx]
    x, y = torch.tensor(batch['binary']), torch.tensor(batch['eval'])
    y_hat = model(x)
    loss = F.l1_loss(y_hat, y)
    print(f'Idx {idx} Eval {y.data[0]:.2f} Prediction {y_hat.data[0]:.2f} Loss {loss:.2f}')
    print(f'FEN {eval.fen}')
    display(SVG(url=svg_url(eval.fen)))


for i in range(5):
    idx = randrange(LABEL_COUNT)
    show_index(idx)
