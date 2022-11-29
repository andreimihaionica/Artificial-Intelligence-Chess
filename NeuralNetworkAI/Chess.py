import chess
import torch
import torch.nn.functional as F

from EvaluationDataset import get_dataset
from EvaluationModel import EvaluationModel
from main import Evaluations
from random import randrange

MATERIAL_LOOKUP = {chess.KING: 0, chess.QUEEN: 9, chess.ROOK: 5, chess.BISHOP: 3, chess.KNIGHT: 3, chess.PAWN: 1}


def avg(lst):
    return sum(lst) / len(lst)


def material_for_board(board):
    eval = 0.0
    for sq, piece in board.piece_map().items():
        mat = MATERIAL_LOOKUP[piece.piece_type]
        if piece.color == chess.BLACK:
            mat = mat * -1
        eval += mat
    return eval


def guess_zero_loss(idx):
    eval = Evaluations.select().where(Evaluations.id == idx + 1).get()
    y = torch.tensor(eval.eval)
    y_hat = torch.zeros_like(y)
    loss = F.l1_loss(y_hat, y)
    return loss


def guess_material_loss(idx):
    eval = Evaluations.select().where(Evaluations.id == idx + 1).get()
    board = chess.Board(eval.fen)
    y = torch.tensor(eval.eval)
    y_hat = torch.tensor(material_for_board(board))
    loss = F.l1_loss(y_hat, y)
    return loss


def guess_model_loss(idx):
    dataset = get_dataset()
    model = EvaluationModel.get_model()
    eval = Evaluations.select().where(Evaluations.id == idx + 1).get()
    batch = dataset[idx]
    x, y = torch.tensor(batch['binary']), torch.tensor(batch['eval'])
    y_hat = model(x)
    loss = F.l1_loss(y_hat, y)
    return loss


LABEL_COUNT = 37164639
zero_losses = []
mat_losses = []
model_losses = []
for i in range(100):
    idx = randrange(LABEL_COUNT)
    zero_losses.append(guess_zero_loss(idx))
    mat_losses.append(guess_material_loss(idx))
    model_losses.append(guess_model_loss(idx))
print(f'Guess Zero Avg Loss {avg(zero_losses)}')
print(f'Guess Material Avg Loss {avg(mat_losses)}')
print(f'Guess Model Avg Loss {avg(model_losses)}')
