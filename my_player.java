import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Stack;

public class my_player {
	
	private final static int BOARD_SIZE = 5;
	private int piece_type;
	private int previous_board[][] = new int[BOARD_SIZE][BOARD_SIZE];
	private int current_board[][] = new int[BOARD_SIZE][BOARD_SIZE];
	private ArrayList<String> possibleMoves = new ArrayList<>();
	
	
	
	
	public void readInput() throws FileNotFoundException {
			
			try {
				BufferedReader br = new BufferedReader(new FileReader("input.txt"));
				piece_type = Integer.parseInt(br.readLine());
				for(int i=0;i<BOARD_SIZE;i++) {
					String row = br.readLine();
					for(int j=0;j<BOARD_SIZE;j++) {
						previous_board[i][j] = Integer.parseInt(row.substring(j, j+1));
					}
				}
				for (int i = 0; i < BOARD_SIZE; i++) {
		            String row = br.readLine();
		            for (int j = 0; j < BOARD_SIZE; j++)
		            	current_board[i][j] = Integer.parseInt(row.substring(j, j+1));
		        }
				
				
			}
			catch(Exception e) {
				System.out.println(e);
			}
			
	}
	
	public ArrayList startWithCenterElement() {
		
		int count = 0;
		boolean startCheck = true;
		for(int i=0;i<BOARD_SIZE;i++) {
			for(int j=0;j<BOARD_SIZE;j++) {
				if(current_board[i][j]!=0) {
					count++;
				}
			}
		}
		
		if((count == 0 && piece_type==1) || (count==1 && current_board[2][2]==0 && piece_type==2)) {
			int row = 2;
			int col = 2;
			possibleMoves.add(2+","+2);
		}
		
		else {
			possibleMoves = minimax(current_board, previous_board,2,-1000,-1000, piece_type);
			
		}
		
		return possibleMoves;
		
	}
	
	public ArrayList valid_moves(int[][] current_board, int [][] previous_board, int piece_type) {
		
		ArrayList<String> validmoves = new ArrayList<>();
		
		for(int i=0;i<BOARD_SIZE;i++) {
			for(int j=0;j<BOARD_SIZE;j++) {
				if(check_for_valid_move(current_board, previous_board, piece_type, i, j))
					validmoves.add(i+","+j);
				
			}
		}
		
		return validmoves;
		
	}
	
	public boolean compare_board(int[][] board1, int[][] board2) {
		for(int i=0;i<BOARD_SIZE;i++) {
			for(int j=0;j<BOARD_SIZE;j++) {
	            if(board1[i][j] != board2[i][j])
	                return false;
			}
		}
	    return true;
	}
	
	
	public ArrayList find_died_pieces(int[][] board,int piece_type) {
		ArrayList<String> died_pieces = new ArrayList<>();
		for(int i=0;i<BOARD_SIZE;i++) {
			for(int j=0;j<BOARD_SIZE;j++) {
	            if(board[i][j] == piece_type) {
	                if((liberty(board, i, j)<=0) && !died_pieces.contains(i+","+j))
	                    died_pieces.add(i+","+j);
	            }
			}
		}
	    return died_pieces;

	}
	
	public int[][] remove_died_pieces(int[][] board, int piece_type) {
		ArrayList<String> died_pieces = find_died_pieces(board, piece_type);
	    if(died_pieces.size()==0)
	        return board;

	    for(String point : died_pieces) {
	    	int xValue = Integer.parseInt(point.substring(0,1));
	    	int yValue = Integer.parseInt(point.substring(2));
	        board[xValue][yValue] = 0;
	    }

	    return board;
	}
	
	public ArrayList find_neighbours(int[][] board, int row, int col) {

		ArrayList<String> neighbors = new ArrayList<>();
	    if(row > 0)
	    	neighbors.add((row-1)+","+col);
	    if(row < board.length - 1)
	    	neighbors.add((row+1)+","+col);
	    
	    if(col > 0)
	    	neighbors.add(row+","+(col-1));
	    
	    if(col < board.length - 1)
	    	neighbors.add(row+","+(col+1));
	    
	    return neighbors;
	
	}
	
	public ArrayList find_neighbor_allies(int[][] board, int row, int col) {
		ArrayList<String> allies = new ArrayList<>();
		
		ArrayList<String>neighbors = find_neighbours(board, row, col);
	    
	    for(String point : neighbors) {
	    	
	    	int xValue = Integer.parseInt(point.substring(0,1));
	    	int yValue = Integer.parseInt(point.substring(2));
	    	
	        if(board[xValue][yValue] == board[row][col])
	            allies.add(point);
	    }

	    return allies;
	    		
	}
	
	public ArrayList minimax(int[][] curr_state, int[][] previous_board, int max_depth, int alpha, int beta, int piece_type) {
	    
	    ArrayList<String> moves = new ArrayList<>();
	    int best = 0;
	    
	    
	    int[][] curr_state_copy = new int[BOARD_SIZE][BOARD_SIZE];
	    
		for(int i=0;i<BOARD_SIZE;i++) {
			for(int j=0;j<BOARD_SIZE;j++) {
				curr_state_copy[i][j] = curr_state[i][j];
			    }
		}	    

	    ArrayList<String> validmoves = new ArrayList<>();
	    validmoves = valid_moves(curr_state, previous_board, piece_type);
		
	    for(String move : validmoves) {
	    	
	    	int[][] next_state = new int[BOARD_SIZE][BOARD_SIZE];
	    	for(int i=0;i<BOARD_SIZE;i++) {
				for(int j=0;j<BOARD_SIZE;j++) {
					next_state[i][j] = curr_state[i][j];
				    }
			}
	    	
	    	int move0 = Integer.parseInt(move.substring(0,1));
	    	int move1 = Integer.parseInt(move.substring(2));
	    	next_state[move0][move1] = piece_type;
	    	next_state = remove_died_pieces(next_state, 3-piece_type);
	    	int utility_value = utility(next_state, 3-piece_type);
	    	int evaluation = minimax2(next_state, curr_state_copy, max_depth, alpha, beta, utility_value, 3-piece_type);
	    	int curr_score = -1 * evaluation;
	    	if(curr_score > best || moves.size()==0) {
	            best = curr_score;
	            alpha = best;
	            moves.clear();
	            moves.add(move);
	    	}
	    	else if(curr_score == best)
	            moves.add(move);
	    	
	    }
	       
	    return moves;
	}
	
	public int minimax2(int[][] curr_state, int[][] previous_board, int max_depth, int alpha, int beta, int utility_value, int next_player) {
	    
		if(max_depth == 0)
	        return utility_value;
	    
		int best = utility_value;

	    int[][] curr_state_copy = new int[BOARD_SIZE][BOARD_SIZE];
	    
		for(int i=0;i<BOARD_SIZE;i++) {
			for(int j=0;j<BOARD_SIZE;j++) {
				curr_state_copy[i][j] = curr_state[i][j];
			    }
		}	    		
	    
	    ArrayList<String> validmoves = new ArrayList<>();
	    validmoves = valid_moves(curr_state, previous_board, next_player);
		
	    for(String move : validmoves) {
		
	        int[][] next_state = new int[BOARD_SIZE][BOARD_SIZE];
	    	for(int i=0;i<BOARD_SIZE;i++) {
				for(int j=0;j<BOARD_SIZE;j++) {
					next_state[i][j] = curr_state[i][j];
				    }
			}		
	        
	    	int move0 = Integer.parseInt(move.substring(0,1));
	    	int move1 = Integer.parseInt(move.substring(2));
	    	next_state[move0][move1] = next_player;
	    	
	        next_state = remove_died_pieces(next_state, 3-next_player);

	        utility_value = utility(next_state, 3-next_player);
	        int evaluation = minimax2(next_state, curr_state_copy, max_depth - 1, alpha, beta, utility_value, 3-next_player);

	        int curr_score = -1 * evaluation;
	        
	        if(curr_score > best)
	            best = curr_score;
	        
	        int new_score = -1 * best;

	        if(next_player == 3-piece_type) {
	            int player = new_score;
	            if(player < alpha)
	                return best;
	            if(best > beta)
	                beta = best;
	        }
	        else if(next_player == piece_type) {
	            int opponent = new_score;
	            if(opponent < beta)
	                return best;
	            if(best > alpha)
	                alpha = best;
	                
	        }
	    }

	    return best;
	    		
	}
	
	public ArrayList dfs(int[][] board, int row, int col) {
	    
		Stack<String> stack = new Stack<>();
		
		stack.push(row+","+col);
		
		ArrayList<String> ally_members = new ArrayList<>();
	    
	    while(!stack.isEmpty()) {
	        String node = stack.pop();
	        ally_members.add(node);
	        
	       ArrayList<String> allies = find_neighbor_allies(board, Integer.parseInt(node.substring(0,1)), Integer.parseInt(node.substring(2)));
	       
	       for(String neighbor : allies) {
	    	    
	    	   if(!stack.contains(neighbor) && !(ally_members.contains(neighbor)))
	                stack.push(neighbor);
	       }
	    }
	    
	    
	    return ally_members;
	
	}
	
	public int liberty(int[][] board, int row, int col) {
	    int count = 0;
	    ArrayList<String> ally_members = dfs(board, row, col);
	    
	    for(String position: ally_members) {
	    	
	    	int xValue = Integer.parseInt(position.substring(0,1));
	    	int yValue = Integer.parseInt(position.substring(2));
	    	
	    	ArrayList<String> neighbors = find_neighbours(board,  xValue, yValue);
	        
	        for(String neighbor : neighbors) {
	        	
	        	int x = Integer.parseInt(neighbor.substring(0,1));
		    	int y = Integer.parseInt(neighbor.substring(2));
	        	
	            if(board[x][y] == 0)
	                count += 1;
	        }
	    }

	    return count;
	}
	
	public boolean check_for_valid_move(int[][] board, int [][] prevboard, int piece_type, int row, int col) {
		   ArrayList<String> dead_pieces = new ArrayList<>();
		   for(int i=0;i<BOARD_SIZE;i++) {
				for(int j=0;j<BOARD_SIZE;j++) {
				            if(previous_board[i][j] == piece_type && current_board[i][j] != piece_type)
				                dead_pieces.add(i+","+j);
				}
		   }
		   if(!(row >= 0 && row < current_board.length))
		        return false;
		    if(!(col >= 0 && col < current_board.length))
		        return false;
		    if(board[row][col] != 0)
		        return false;
		    
		    int[][] currboard_copy = new int[BOARD_SIZE][BOARD_SIZE];
		    
		    for(int i=0;i<BOARD_SIZE;i++) {
		    	for(int j=0;j<BOARD_SIZE;j++) {
		    		currboard_copy[i][j] = board[i][j];
		    	}
		    }
		    
		    currboard_copy[row][col] = piece_type;
		    
		    if(liberty(currboard_copy, row, col) >= 1)
		        return true;

		    currboard_copy = remove_died_pieces(currboard_copy, 3 - piece_type);

		    if(liberty(currboard_copy, row, col) < 1)
		        return false;

		    else {
		        if(dead_pieces.size()>0 && compare_board(previous_board, currboard_copy))
		            return false; 
		        return true; 
		    }

	}
	
	public void writeOutput(String move) {
		try {
		      FileWriter myWriter = new FileWriter("output.txt");
		      myWriter.write(move);
		      myWriter.close();
		    } catch (IOException e) {
		      e.printStackTrace();
		    }
	}
	
	public int utility(int[][] board, int piece) {
	    
		int myself = 0;
		int TA = 0;
		int h_me = 0;
		int hTA = 0;
		int utility;
		for(int i=0;i<BOARD_SIZE;i++) {
			for(int j=0;j<BOARD_SIZE;j++) {
				if(board[i][j]==piece_type) {
					myself += 1;
					h_me += (myself + liberty(board, i ,j));
				}
				else if(board[i][j] == 3- piece_type) {
					TA += 1;
					hTA += (TA + liberty(board, i, j));
				}
			}
		}
	    if(piece == piece_type)
	        utility =  h_me - hTA;
	    else 
	    	utility =  hTA - h_me;
	    
	    //System.out.println(utility);
	    return utility;
	    
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		
		my_player m1 = new my_player();
		String action = null;
		m1.readInput();
		ArrayList<String> moves = m1.startWithCenterElement();
		if(moves.size()==0) {
			action = "PASS";
		}
		else {
			Random rand = new Random();
			int randomIndex = rand.nextInt(moves.size());
	        action = moves.get(randomIndex);
		}
		
		m1.writeOutput(action);
		
	}
	
}
