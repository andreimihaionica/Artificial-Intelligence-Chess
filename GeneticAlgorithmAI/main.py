from fitness import *
from genetic_algorithm import *
from network_evaluation import *

eval_model = simple_eval()
tourno_fitness = fitness

ga = GeneticAlgorithm.genetic_algorithm()
agent, loss = ga.execute(tourno_fitness, eval_model, pop_size=10, generations=10)
print(agent.fitness)

board = chess.Board()
g
for move in agent.game:
    board.push_san(move)
print(board)
print(board.is_checkmate())
print(agent.game)
