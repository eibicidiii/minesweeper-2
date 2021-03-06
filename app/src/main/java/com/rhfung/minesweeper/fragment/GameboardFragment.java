package com.rhfung.minesweeper.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.text.Layout;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import com.rhfung.minesweeper.R;
import com.rhfung.minesweeper.model.GameModel;
import com.rhfung.minesweeper.view.GameCell;

/**
 * GameboardFragment shows the gameboard plus game controls New Game and Validate.
 */
public class GameboardFragment extends Fragment {

    private View rootView;
    private AbsoluteLayout tableGrid;
    private GameCell[][] gameBoard;
    private GameModel gameModel;
    private Button newGameButton;
    private Button validateButton;

    public GameboardFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_gameboard, container, false);
        tableGrid = (AbsoluteLayout) rootView.findViewById(R.id.gameboard);
        gameModel = new GameModel();
        newGameButton = (Button) rootView.findViewById(R.id.new_game_button);
        validateButton = (Button) rootView.findViewById(R.id.validate_game_button);

        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newGame();
            }
        });

        validateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer();
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        renderGrid();
    }

    private GameCell getCellAt(int row, int col)  {
        return gameBoard[row][col];
    }

    /**
     * Renders the grid after tableGrid is loaded
     */
    private void renderGrid() {
        gameBoard = new GameCell[GameModel.BOARD_HEIGHT][];
        Context context = tableGrid.getContext();

        tableGrid.removeAllViews();

        // get the screen size to compute cell height
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point screenSize = new Point();
        display.getSize(screenSize);

        // add in some cell padding
        int cellWidth = Math.min(screenSize.x, screenSize.y) / (GameModel.BOARD_WIDTH + 1);

        for (int i = 0; i < GameModel.BOARD_HEIGHT; i++) {
            gameBoard[i] = new GameCell[GameModel.BOARD_WIDTH];

            for (int j = 0; j < GameModel.BOARD_WIDTH; j++) {
                GameCell cell = new GameCell(context);
                cell.setPositionAndValue(i, j, gameModel.getGameBoardAt(i, j));

                tableGrid.addView(cell);
                AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(cellWidth,
                        cellWidth, cellWidth * i, cellWidth * j);
                cell.setLayoutParams(params);

                gameBoard[i][j] = cell;
                cell.setOnClickListener(cellClicked);
            }
        }
    }

    /**
     * Reveal the location of all mines without ending the game.
     */
    public void cheatAndRevealMines() {
        for (int r = 0; r < GameModel.BOARD_HEIGHT; r++) {
            for (int c = 0; c < GameModel.BOARD_WIDTH; c++) {
                getCellAt(r, c).cheatAndRevealMine();
            }
        }
    }

    /**
     * Reveal the full game board and end the game.
     */
    private void revealFullBoard() {
        for (int r = 0; r < GameModel.BOARD_HEIGHT; r++) {
            for (int c = 0; c < GameModel.BOARD_WIDTH; c++) {
                getCellAt(r, c).setRevealed(true);
            }
        }
    }

    /**
     * Reveal the surrounding cells for (row, col), which should have 0 mines recorded in it.
     * @param row
     * @param col
     */
    private void revealAround(int row, int col) {
        for (int r = Math.max(row - 1, 0); r <= Math.min(row + 1, GameModel.BOARD_HEIGHT - 1); r++) {
            for (int c = Math.max(col - 1, 0); c <= Math.min(col + 1, GameModel.BOARD_WIDTH - 1); c++) {
                getCellAt(r,c).setRevealed(true);
            }
        }
    }

    /**
     * Start a new game board.
     */
    private void newGame() {
        gameModel = new GameModel();
        renderGrid();
    }

    /**
     * Checks the validity of the game and tells the user if he/she won.
     */
    private void checkAnswer() {
        boolean solution = gameModel.validateGameSolution(gameBoard);
        if (solution) {
            // game over
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.won_title)
                    .setMessage(R.string.won_body)
                    .setNeutralButton(R.string.game_over_button,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // intentionally left blank
                                }
                            }).show();
        } else {
            // game over, reveal all mines
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.lost_title)
                    .setMessage(R.string.lost_body)
                    .setNeutralButton(R.string.game_over_button,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    revealFullBoard();
                                }
                            }).show();
        }
    }

    /**
     * Handles when any cell is pressed.
     */
    private View.OnClickListener cellClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onCellClicked((GameCell) v);
        }

        public void onCellClicked(GameCell cell) {
            // don't do anything if the cell is already revealed
            if (cell.isRevealed()) {
                return;
            }

            cell.setRevealed(true);
            int row = cell.getRow();
            int col = cell.getColumn();

            if (gameModel.getGameBoardAt(row, col) == GameModel.MINE) {
                // game over, reveal all mines
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.game_over_title)
                        .setMessage(R.string.game_over_body)
                        .setNeutralButton(R.string.game_over_button,
                                new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // reveal the board
                                revealFullBoard();
                            }
                        }).show();
            }
            else if (gameModel.getGameBoardAt(row, col) == 0) {
                revealAround(row, col);
            }
        }
    };
}