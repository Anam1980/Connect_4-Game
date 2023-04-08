package com.example.connect4game;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;


import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable {

    private static final int COLUMNS = 7;
    private static final int ROWS = 6;
    private static final int CIRCLE_DIAMETER = 80;
    private static final String discColor1 = "#24303E";
    private static final String discColor2 = "#4CAA88";


    private static String PLAYER_ONE = "Player One";
    private static String PLAYER_TWO = "Player Two";

    private  boolean isPlayerOneTurn = true;

    private Disc[][] insertedDiscsArray = new Disc[ROWS][COLUMNS]; // for structural changes






    @FXML
    public GridPane rootGridPane;

    @FXML
    public Pane insertedDiscsPane;

    @FXML
    public Label playerNameLabel;

    @FXML
    public TextField playerOneName;

    @FXML
    public TextField playerTwoName;

    @FXML
    public Button setNamesButton;

    private boolean isAllowedToInsert = true; // Flag to avoid adding discs from the same color right after each other

    public void createPlayground() {

        Shape rectangleWithHoles = createGameStructuralGrid();
        rootGridPane.add(rectangleWithHoles, 0, 1); // adding the rectangleWithHoles to the Grid Pane

        List<Rectangle> rectangleList = createClickableColumns();

        for (Rectangle rectangle: rectangleList) { //creating blue rectangles for all the columns
            rootGridPane.add(rectangle, 0, 1);
        }
    }

    private Shape createGameStructuralGrid() {
        Shape rectangleWithHoles = new Rectangle((COLUMNS + 1) * CIRCLE_DIAMETER, (ROWS + 1) * CIRCLE_DIAMETER); // adding an extra to ROW and COLUMNS providing us with margin in UI

        for(int row = 0; row < ROWS; row++) {  // nested for loop to create the rows and columns of circles. Because it is like a two day array we use nested for loop
            for (int col = 0; col < COLUMNS; col ++) {

                Circle circle = new Circle();
                circle.setRadius(CIRCLE_DIAMETER / 2);
                circle.setCenterX(CIRCLE_DIAMETER / 2);
                circle.setCenterY(CIRCLE_DIAMETER / 2);
                circle.setSmooth(true); // smoothing the edges of the holes

                circle.setTranslateX(col * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4);  // plus 5 makes some space between the circles
                circle.setTranslateY(row * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4);  // the + CIRC_DIA / 4 is actually 20, it gives some space on the edges

                rectangleWithHoles = Shape.subtract(rectangleWithHoles, circle); //a meglevo feher negyzetbol kivonjuk a kor format igy megjelenik a feher negyszog mogotti hatter szin egy kor formajaban
            }
        }

        rectangleWithHoles.setFill(Color.WHITE);

        return rectangleWithHoles;

    }

    private  List<Rectangle> createClickableColumns() {

        List<Rectangle> rectangleList = new ArrayList<>();

        for (int col = 0; col < COLUMNS; col++) {

            Rectangle rectangle = new Rectangle(CIRCLE_DIAMETER, (ROWS + 1) * CIRCLE_DIAMETER);
            rectangle.setFill(Color.TRANSPARENT);
            rectangle.setTranslateX(col * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4);  // applying margin with TranslateAnimation, so the blue rectangle appears exactly on top of the holes

            rectangle.setOnMouseEntered(event -> rectangle.setFill(Color.valueOf("eeeeee26"))); // it will set the color to the desired color if we hoover over the rectangle
            rectangle.setOnMouseExited(event -> rectangle.setFill(Color.TRANSPARENT)); // when we move the courdor away from rect, it will revert the color back to the original color

            final  int column = col; // the var used in Lambda expression should be final or effectivelly final
            rectangle.setOnMouseClicked(event -> {
                if(isAllowedToInsert) {
                    isAllowedToInsert = false; // when disc is being dropped then no more disc will be inserted

                    insertDisc(new Disc(isPlayerOneTurn), column);
                }
            });

            rectangleList.add(rectangle);
        }
        return rectangleList;
    }

    private void  insertDisc(Disc disc, int column) {

        int row = ROWS - 1;
        while (row >= 0) {

            if (getDiscIfPresent(row, column) == null)
                break;

            row--;
        }

        if( row < 0)  // if the row is full, we can not onsert anymore discs
            return;  // so simply do nothing

        insertedDiscsArray[row][column] = disc; // for structural chnages, for developers
        insertedDiscsPane.getChildren().add(disc); // for players. this is the 2nd pane

        disc.setTranslateX(column * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4);  //ezzel a sorral a fekete disc annak a sornak a tetejen jelenik meg, amelyiktre kattintunk

        int currentRow = row;
        TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.5), disc);
        translateTransition.setToY(row * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4);  // this line makes the disc to fall to the bottom
        translateTransition.setOnFinished(event -> {

            isAllowedToInsert = true; // finally when the disc is droped, we can allow the next player to insert doisc
            if (gameEnded( currentRow, column)) {
                gameOver();
            }

            isPlayerOneTurn = !isPlayerOneTurn;  //with this line we toggle over to playerTwo

            playerNameLabel.setText(isPlayerOneTurn? PLAYER_ONE : PLAYER_TWO);
        });

        translateTransition.play(); //


    }

    private boolean gameEnded(int row, int column) {

        List<Point2D> verticalPoints = IntStream.rangeClosed(row - 3, row + 3)  // range of row values = 0,1,2,3,4,5
                .mapToObj(r -> new Point2D(r, column))  //0,3 1,3 2,3 3,3 4,3 5,3 --> Point2D class in java holds values for x and y coordinate
                .collect(Collectors.toList());

        List<Point2D> horizontalPoints = IntStream.rangeClosed(column - 3, column + 3)
                .mapToObj(c -> new Point2D(row, c))
                .collect(Collectors.toList());

        Point2D startPoint1 = new Point2D(row - 3, column + 3);
        List<Point2D> diagonal1Points = IntStream.rangeClosed(0, 6)
                .mapToObj(i -> startPoint1.add(i, -i))
                .collect(Collectors.toList());

        Point2D startPoint2 = new Point2D(row - 3, column - 3);
        List<Point2D> diagonal2Points = IntStream.rangeClosed(0, 6)
                .mapToObj(i -> startPoint2.add(i, i))
                .collect(Collectors.toList());



        boolean isEnded = checkCombinations(verticalPoints) || checkCombinations(horizontalPoints)
                || checkCombinations(diagonal1Points) || checkCombinations(diagonal2Points);

        return isEnded;
    }

    private boolean checkCombinations(List<Point2D> points) {

        int chain = 0;

        for ( Point2D point : points) {

            int rowIndexForArray = (int) point.getX(); //get the row index form point object
            int columnIndexForArray = (int) point.getY();

            Disc disc = getDiscIfPresent(rowIndexForArray, columnIndexForArray);

            if(disc != null && disc.isPlayerOneMove == isPlayerOneTurn) { //if the last inserted disc belongs to the  current player

                chain++;
                if (chain == 4) {
                    return true;
                }
            } else {
                chain = 0;
            }
        }

        return false;
    }

    private Disc getDiscIfPresent(int row, int column) {  // to prevent ArrayOutOfBoundException

        if (row >= ROWS || row < 0 || column >= COLUMNS || column < 0) // if roe or column  index is invalid
            return null;

        return insertedDiscsArray[row][column];

    }

    private void gameOver() {
        String winner = isPlayerOneTurn ? PLAYER_ONE : PLAYER_TWO;
        System.out.println("The victory is: " + winner);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Connect Four");
        alert.setHeaderText("The victory is " + winner);
        alert.setContentText("Would you like to play again? ");

        ButtonType yesBtn = new ButtonType("Yes");
        ButtonType noBtn = new ButtonType("No, Exit");
        alert.getButtonTypes().setAll(yesBtn, noBtn); // adding these 2 buttons to the alert dialog

        Platform.runLater(()-> {
            Optional<ButtonType> btnClicked = alert.showAndWait();
            if (btnClicked.isPresent() && btnClicked.get() == yesBtn) { // without isPresent() the app can crash. It is showed in warning by intellijiIDEA
                // ...user chose Yes -> Reset the game
                resetGame();
            } else {
                // No -> Exit game
                Platform.exit();
                System.exit(0);
            }
        });
    }

    public void resetGame() {
        insertedDiscsPane.getChildren().clear(); // Remove inserted discs from pane

        for (int row = 0; row < insertedDiscsArray.length; row++) { // make all the elements of the insertedDiscArray null
            for (int col = 0; col < insertedDiscsArray[row].length; col++) {
                insertedDiscsArray[row][col] = null;
            }
        }
        isPlayerOneTurn = true; // let player one start the game
        playerNameLabel.setText(PLAYER_ONE);

        createPlayground();
    }

    private static class Disc extends Circle {

        private final boolean isPlayerOneMove;

        public Disc(boolean isPlayerOneMove) {
            this.isPlayerOneMove = isPlayerOneMove;
            setRadius(CIRCLE_DIAMETER / 2);
            setFill(isPlayerOneMove? Color.valueOf(discColor1) : Color.valueOf(discColor2));
            setCenterX(CIRCLE_DIAMETER / 2);
            setCenterY(CIRCLE_DIAMETER / 2);
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        setNamesButton.setOnAction(event -> {

            String input1 = playerOneName.getText();
            String input2 = playerTwoName.getText();

            PLAYER_ONE = input1 + "`s";
            PLAYER_TWO = input2 + "`s";

            if (input1.isEmpty())
                PLAYER_ONE = "Player One`s";

            if (input2.isEmpty())
                PLAYER_TWO = "Player Two`s";

            //  isPlayerOneTurn = !isPlayerOneTurn;
            playerNameLabel.setText(isPlayerOneTurn? PLAYER_ONE : PLAYER_TWO);

        });


    }
}
