import os
import copy
import time
import random

# define all the constant variables
BOARD_SIZE = 5
INPUT = 'input.txt'
OUTPUT = 'output.txt'
test_input = "test_input.txt"

# function built to read in input.txt and update the previous and current boards
def read_input(input_file):
    input_info = list()
    with open(input_file, 'r') as F:
        for line in F.readlines():
            input_info.append(line.strip())

    piece_type = int(input_info[0])
    previous_board = [[int(val) for val in line] for line in input_info[1:BOARD_SIZE+1]]
    board = [[int(val) for val in line] for line in input_info[BOARD_SIZE+1: 2*BOARD_SIZE+1]]

    return piece_type, board, previous_board

# function that writes output file
def write_output(output_file, move):
    with open(output_file, 'w') as F:
        if move == 'PASS':
            F.write(move)
        else:
           F.write(str(move[0])+','+str(move[1]))

def utility(board, piece):
    myself, TA, h_me, hTA = 0, 0, 0, 0
    utility = 0
    for i in range(BOARD_SIZE):
        for j in range(BOARD_SIZE):
            if board[i][j] == piece_type:
                myself += 1
                h_me += (myself + liberty(board, i, j))
            elif board[i][j] == 3 - piece_type:
                TA += 1
                hTA += (TA + liberty(board, i, j))

    if piece == piece_type:
        utility = h_me - hTA
    else:
        utility = hTA - h_me
    return utility

# finds dead stones given stone color
def find_died_pieces(board, piece_type):
    died_pieces = []
    for i in range(BOARD_SIZE):
        for j in range(BOARD_SIZE):
            if board[i][j] == piece_type:
                if not liberty(board, i, j) and (i,j) not in died_pieces:
                    died_pieces.append((i, j))
    return died_pieces

# given stone color, removes dead stones
def remove_died_pieces(board, piece_type):
    died_pieces = find_died_pieces(board, piece_type)
    if not died_pieces:
        return board

    for stone in died_pieces:
        board[stone[0]][stone[1]] = 0

    return board

# function that removes dead stones and returns adjacent stones within gameboard range
def find_neighbours(board, row, col):
    #neighbors = [(row - 1, col),
    #            (row + 1, col),
    #            (row, col - 1),
    #            (row, col + 1)]
    #return ([point for point in neighbors if 0 <= point[0] < BOARD_SIZE and 0 <= point[1] < BOARD_SIZE])
    neighbors = []
    if row > 0: neighbors.append((row-1, col))
    if row < len(board) - 1: neighbors.append((row+1, col))
    if col > 0: neighbors.append((row, col-1))
    if col < len(board) - 1: neighbors.append((row, col+1))
    return neighbors

# Function that returns list of all adjacent ally stones given another stones position
def find_neighbor_allies(board, row, col):
    allies = list()
    vari = [(row,col)]
    #print(vari)
    neighbors = find_neighbours(board, row, col)
    #print(neighbors)
    for point in neighbors:
        if board[point[0]][point[1]] == board[row][col]:
            allies.append(point)


    return allies

# function that returns ally cluster of a point Implemented using BFS and above function
# returns a list of ally cluster given a certain point on the board
def dfs(board, row, col):
    
    stack = [(row, col)]
    ally_members = list()
    
    while stack:
        node = stack.pop(0)
        ally_members.append(node)
        # if ally nieghbors not empty, add them to cluster_dict
        allies = find_neighbor_allies(board, node[0], node[1])
        
        for neighbor in allies:
            if neighbor not in stack and neighbor not in ally_members:
                stack.append(neighbor)
    
    return ally_members

def liberty(board, row, col):
    count = 0
    # loop through each point in the cluster
    ally_members = dfs(board, row, col) 
    
    for piece in ally_members:
        # if the point has an adjacent node with a value of 0, then the cluster has liberty
        neighbors = find_neighbours(board,  piece[0], piece[1])
        for neighbor in neighbors:
            if board[neighbor[0]][neighbor[1]] == 0:
                count += 1

    return count

def compare_board(board1, board2):
    for i in range(5):
        for j in range(5):
            if board1[i][j] != board2[i][j]:
                return False
    return True

def check_for_valid_move(board, previous_board, player, row, col):

    dead_pieces = []
    for i in range(5):
        for j in range(5):
            if previous_board[i][j] == player and board[i][j] != player:
                dead_pieces.append((i, j))

    if not (row >= 0 and row < len(board)):
        return False
    if not (col >= 0 and col < len(board)):
        return False
    if board[row][col] != 0:
        return False
    board_copy = copy.deepcopy(board)
    board_copy[row][col] = player


    if liberty(board_copy, row, col) >= 1:
        return True

    board_copy = remove_died_pieces(board_copy, 3 - player)

    if not liberty(board_copy, row, col) >= 1:
        return False

    else:
        if dead_pieces and compare_board(previous_board, board_copy):
            return False 
        return True 

# return a list of valid moves given current gameboard position
def valid_moves(board, previous_board, player):
    valid_moves = list()
    # loop through the entire gameboard
    for i in range(BOARD_SIZE):
        for j in range(BOARD_SIZE):
            # position that has a 0 is empty
            if check_for_valid_move(board, previous_board, player, i, j) == True:
                valid_moves.append((i,j))
    
    return valid_moves

# the main minimax function
def minimax(curr_state, previous_board, max_depth, alpha, beta, piece_type):
    # intialize helper variables
    moves = list()
    best = 0
    #game_state_copy = copy.deepcopy(game_state)
    #next_state = copy.deepcopy(curr_state)
    curr_state_copy = copy.deepcopy(curr_state)

    # iterate through all valid moves
    for move in valid_moves(curr_state, previous_board, piece_type):
        # update the next state board
        next_state = copy.deepcopy(curr_state)
        next_state[move[0]][move[1]] = piece_type
        next_state = remove_died_pieces(next_state, 3-piece_type)

        # get heuristic of next state
        utility_value = utility(next_state, 3-piece_type)
        evaluation = minimax2(next_state, curr_state_copy, max_depth, 
                                    alpha, beta, utility_value, 3-piece_type)

        curr_score = -1 * evaluation

        # check if moves is empty or if we have new best move(s)
        if curr_score > best or not moves:
            best = curr_score
            alpha = best
            moves = [move]
        # if we have another best move then we add to the moves list
        elif curr_score == best:
            moves.append(move)

    return moves

# the second minimax 'helper' function that iterates through the branches
def minimax2(curr_state, previous_board, max_depth, alpha, beta, utility_value, next_player):
    if max_depth == 0:
        return utility_value
    best = utility_value

    curr_state_copy = copy.deepcopy(curr_state)
    # iterate through all valid moves
    for move in valid_moves(curr_state, previous_board, next_player):
        # update the next board state
        next_state = copy.deepcopy(curr_state)
        next_state[move[0]][move[1]] = next_player
        next_state = remove_died_pieces(next_state, 3-next_player)

        # get heuristic of next state
        utility_value = utility(next_state, 3-next_player)
        evaluation = minimax2(next_state, curr_state_copy, max_depth - 1, 
                                    alpha, beta, utility_value, 3-next_player)

        curr_score = -1 * evaluation
        # check if new result is better than current best
        if curr_score > best:
            best = curr_score
        # update the score
        new_score = -1 * best

        # Alpha beta pruning
        # if we are looking at the minimizing player (opponent)
        if next_player == 3-piece_type:
            player = new_score
            # check if prune
            if player < alpha:
                return best
            # if we dont prune, update beta
            if best > beta:
                beta = best
        # if we are looking at the maximizing player (ourselves)
        elif next_player == piece_type:
            opponent = new_score
            # check prune
            if opponent < beta:
                return best
            # if we dont prune, update alpha
            if best > alpha:
                alpha = best

    return best

start = time.time()
piece_type, cur_board, pre_board = read_input(INPUT)

checker=0
checker_bool = False
for i in range(5):
    for j in range(5):
        if cur_board[i][j] != 0:
            if i == 2 and j == 2:
                checker_bool = True
            checker += 1

if (checker==0 and piece_type==1) or (checker==1 and piece_type==2 and checker_bool is False):
    action = [(2,2)]
else:
    action = minimax(cur_board, pre_board, 2, -1000, -1000, piece_type)
    print(action)
# if empty list, then no action, choose to pass
if action == []:
    rand_action = ['PASS']
# else choose a random action from the list
else:
    rand_action = random.choice(action)

write_output(OUTPUT, rand_action)
end = time.time()
print(f'total time of evaluation: {end-start}')