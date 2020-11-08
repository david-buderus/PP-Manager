package ui.map;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import model.map.Map;
import model.map.object.IPosition;
import model.map.object.loot.LootObject;

public class MapCanvas extends Pane implements IMapCanvas {

    private final Canvas mapCanvas, infoCanvas;
    private final GraphicsContext mapContext, infoContext;
    private final ObjectProperty<Map> map;

    private double offsetX, offsetY;
    private double prevX, prevY;
    private double zoom;

    private final IntegerProperty shownYLayer;

    public MapCanvas(ObjectProperty<Map> map, IntegerProperty shownYLayer) {
        this.mapCanvas = new Canvas(300, 300);
        this.infoCanvas = new Canvas(300, 300);
        this.mapContext = mapCanvas.getGraphicsContext2D();
        this.infoContext = infoCanvas.getGraphicsContext2D();

        this.offsetX = 0;
        this.offsetY = 0;
        this.zoom = 10;

        this.map = map;
        this.shownYLayer = shownYLayer;
        this.shownYLayer.addListener((ob, o, n) -> refresh());

        StackPane stack = new StackPane();
        stack.getChildren().add(mapCanvas);
        stack.getChildren().add(infoCanvas);
        this.getChildren().add(stack);

        mapCanvas.widthProperty().bind(this.widthProperty());
        mapCanvas.heightProperty().bind(this.heightProperty());
        infoCanvas.widthProperty().bind(this.widthProperty());
        infoCanvas.heightProperty().bind(this.heightProperty());

        this.setOnMousePressed(event -> {
            prevX = event.getX();
            prevY = event.getY();
        });
        this.setOnMouseDragged(event -> {
            double x = event.getX();
            double y = event.getY();
            moveOffset((prevX - x) / zoom, (prevY - y) / zoom);
            prevX = x;
            prevY = y;
        });
        this.setOnScroll(event -> {
            zoom += event.getDeltaY() / 100;
            moveOffset(event.getDeltaY() / 200, event.getDeltaY() / 200);
            refresh();
        });
        this.setOnMouseMoved(event -> {
            infoContext.save();
            infoContext.clearRect(0, 0, infoCanvas.getWidth(), infoCanvas.getHeight());
            if (map.get() != null) {
                int mapX = (int) (event.getX() / zoom + offsetX);
                int mapZ = (int) (event.getY() / zoom + offsetY);

                this.drawInfoHud(event.getX(), event.getY(), mapX, mapZ);
            }
            infoContext.restore();
        });
    }

    public void refresh() {
        clear();
        if (map.get() != null) {
            map.get().draw(this);
        }
    }

    public void clear() {
        infoContext.clearRect(0, 0, infoCanvas.getWidth(), infoCanvas.getHeight());
        mapContext.clearRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());
    }

    @Override
    public void drawRectangle(int x, int y, int z, int width, int depth, int rotation) {
        drawRectangle(x, y, z, width, depth, rotation, Color.BLACK);
    }

    @Override
    public void drawRectangle(int x, int y, int z, int width, int depth, int rotation, Color color) {
        if (outOfBounds(x, y, z, width, 1, depth, rotation)) {
            return;
        }

        double canvasX = (x - offsetX) * zoom;
        double canvasY = (z - offsetY) * zoom;

        mapContext.save();
        mapContext.setFill(color);
        if (rotation % 2 == 0) {
            mapContext.fillRect(canvasX, canvasY, width * zoom, depth * zoom);
        } else {
            mapContext.fillRect(canvasX, canvasY, depth * zoom, width * zoom);
        }
        mapContext.restore();
    }

    @Override
    public void drawImage(Image image, int x, int y, int z, int width, int depth, int rotation) {
        drawImage(image, 0, 0, image.getWidth(), image.getHeight(),
                x, y, z, width, depth, rotation);
    }

    @Override
    public void drawImage(Image image, double sourceX, double sourceY, double sourceWidth, double sourceHeight,
                          int x, int y, int z, int width, int depth, int rotation) {
        if (outOfBounds(x, y, z, width, 1, depth, rotation)) {
            return;
        }

        double canvasX = (x - offsetX) * zoom;
        double canvasY = (z - offsetY) * zoom;

        mapContext.save();
        switch (rotation) {
            case 1:
                canvasX += depth * zoom;
                break;
            case 2:
                canvasX += width * zoom;
                canvasY += depth * zoom;
                break;
            case 3:
                canvasY += width * zoom;
                break;
        }

        mapContext.transform(new Affine(new Rotate(rotation * 90, canvasX, canvasY)));
        mapContext.drawImage(image, sourceX, sourceY, sourceWidth, sourceHeight, canvasX, canvasY, width * zoom, depth * zoom);
        mapContext.restore();
    }

    @Override
    public void drawImage(Image image, IPosition position) {
        drawImage(image, position.getX(), position.getY(), position.getZ(), position.getWidth(), position.getDepth(), position.getRotation());
    }

    @Override
    public void drawImage(Image image, IPosition position, int offsetX, int offsetY, int offsetZ, int width, int depth, int rotation) {
        drawImage(image, 0, 0, image.getWidth(), image.getHeight(),
                position, offsetX, offsetY, offsetZ, width, depth, rotation);
    }

    @Override
    public void drawImage(Image image, double sourceX, double sourceY, double sourceWidth, double sourceHeight,
                          IPosition position) {
        drawImage(image, sourceX, sourceY, sourceWidth, sourceHeight,
                position.getX(), position.getY(), position.getZ(), position.getWidth(), position.getDepth(), position.getRotation());
    }

    @Override
    public void drawImage(Image image, double sourceX, double sourceY, double sourceWidth, double sourceHeight,
                          IPosition position, int offsetX, int offsetY, int offsetZ, int width, int depth, int rotation) {
        if (outOfBounds(position.getX(), position.getY(), position.getZ(), position.getWidth(), 1, position.getDepth(), position.getRotation())) {
            return;
        }

        double canvasX = (position.getX() - this.offsetX) * zoom;
        double canvasY = (position.getZ() - this.offsetY) * zoom;

        mapContext.save();
        switch (position.getRotation()) {
            case 1:
                canvasX += position.getDepth() * zoom;
                break;
            case 2:
                canvasX += position.getWidth() * zoom;
                canvasY += position.getDepth() * zoom;
                break;
            case 3:
                canvasY += position.getWidth() * zoom;
                break;
        }
        mapContext.transform(new Affine(new Rotate(position.getRotation() * 90, canvasX, canvasY)));

        canvasX += offsetX * zoom;
        canvasY += offsetZ * zoom;

        mapContext.transform(new Affine(new Rotate(rotation * 90, canvasX, canvasY)));

        switch (rotation) {
            case 1:
                canvasY -= width * zoom;
                break;
            case 2:
                canvasX -= width * zoom;
                canvasY -= width * zoom;
                break;
            case 3:
                canvasX -= width * zoom;
                break;
        }

        mapContext.drawImage(image, sourceX, sourceY, sourceWidth, sourceHeight, canvasX, canvasY, width * zoom, depth * zoom);
        mapContext.restore();
    }

    @Override
    public void drawPerspectiveImage(Image image, int x, int y, int z, int width, int height, int depth, int rotation) {
        drawPerspectiveImage(image, 0, 0, image.getWidth() / 4, image.getHeight() / height,
                x, y, z, width, height, depth, rotation);
    }

    @Override
    public void drawPerspectiveImage(Image image, double sourceX, double sourceY, double sourceWidth, double sourceHeight,
                                     int x, int y, int z, int width, int height, int depth, int rotation) {
        if (outOfBounds(x, y, z, width, height, depth, rotation)) {
            return;
        }

        int usingHeight = shownYLayer.get() - y;

        double canvasX = (x - offsetX) * zoom;
        double canvasY = (z - offsetY) * zoom;

        sourceX += sourceWidth * ((rotation + 4) % 4);
        sourceY += sourceHeight * usingHeight;
        mapContext.drawImage(image, sourceX, sourceY, sourceWidth, sourceHeight, canvasX, canvasY, width * zoom, depth * zoom);
    }

    @Override
    public void drawPerspectiveImage(Image image, IPosition position) {
        int size = Math.max(position.getWidth(), position.getDepth());
        drawPerspectiveImage(image, position.getX(), position.getY(), position.getZ(), size, position.getHeight(), size, position.getRotation());
    }

    @Override
    public void drawPerspectiveImage(Image image, IPosition position, int offsetX, int offsetY, int offsetZ, int width, int height, int depth, int rotation) {
        drawPerspectiveImage(image, 0, 0, image.getWidth() / 4, image.getHeight() / height,
                position, offsetX, offsetY, offsetZ, width, height, depth, rotation);
    }

    @Override
    public void drawPerspectiveImage(Image image, double sourceX, double sourceY, double sourceWidth, double sourceHeight,
                                     IPosition position) {
        drawPerspectiveImage(image, sourceX, sourceY, sourceWidth, sourceHeight,
                position.getX(), position.getY(), position.getZ(), position.getWidth(), position.getHeight(), position.getDepth(), position.getRotation());
    }

    @Override
    public void drawPerspectiveImage(Image image, double sourceX, double sourceY, double sourceWidth, double sourceHeight,
                                     IPosition position, int offsetX, int offsetY, int offsetZ, int width, int height, int depth, int rotation) {
        if (outOfBounds(position.getX(), position.getY() + offsetY, position.getZ(), position.getWidth(), height, position.getDepth(), position.getRotation())) {
            return;
        }

        double canvasX = (position.getX() - this.offsetX) * zoom;
        double canvasY = (position.getZ() - this.offsetY) * zoom;
        int usingHeight = shownYLayer.get() - (position.getY() + offsetY);

        mapContext.save();
        switch (position.getRotation()) {
            case 1:
                canvasX += position.getDepth() * zoom;
                break;
            case 2:
                canvasX += position.getWidth() * zoom;
                canvasY += position.getDepth() * zoom;
                break;
            case 3:
                canvasY += position.getWidth() * zoom;
                break;
        }
        mapContext.transform(new Affine(new Rotate(position.getRotation() * 90, canvasX, canvasY)));

        canvasX += offsetX * zoom;
        canvasY += offsetZ * zoom;

        mapContext.transform(new Affine(new Rotate(position.getRotation() * -90, canvasX, canvasY)));

        switch (position.getRotation()) {
            case 1:
                canvasX -= width * zoom;
                break;
            case 2:
                canvasX -= width * zoom;
                canvasY -= depth * zoom;
                break;
            case 3:
                canvasY -= width * zoom;
                break;
        }

        int size = Math.max(width, height);
        sourceX += sourceWidth * ((((rotation + position.getRotation()) % 4) + 4) % 4);
        sourceY += sourceHeight * usingHeight;
        mapContext.drawImage(image, sourceX, sourceY, sourceWidth, sourceHeight, canvasX, canvasY, size * zoom, size * zoom);
        mapContext.restore();
    }

    public double getOffsetY() {
        return offsetY;
    }

    public double getOffsetX() {
        return offsetX;
    }

    public void setOffset(double offsetX, double offsetY) {
        if (offsetX < 0) {
            this.offsetX = 0;
        } else {
            this.offsetX = offsetX;
        }
        if (offsetY < 0) {
            this.offsetY = 0;
        } else {
            this.offsetY = offsetY;
        }
        refresh();
    }

    public void moveOffset(double offsetX, double offsetY) {
        setOffset(getOffsetX() + offsetX, getOffsetY() + offsetY);
    }

    private boolean outOfBounds(int x, int y, int z, int width, int height, int depth, int rotation) {
        if (shownYLayer.get() < y || y + height <= shownYLayer.get()) {
            return true;
        }
        if (rotation % 2 == 0) {
            if (x + width < offsetX || z + depth < offsetY) {
                return true;
            }
        } else {
            if (x + depth < offsetX || z + width < offsetY) {
                return true;
            }
        }
        return mapCanvas.getWidth() / zoom + offsetX < x || mapCanvas.getHeight() / zoom + offsetY < z;
    }

    private void drawInfoHud(double x, double y, int mapX, int mapZ) {
        LootObject loot = map.get().getLootObject(mapX, shownYLayer.get(), mapZ);
        if (loot != null) {
            String infoText = loot.getInfoText();
            long lines = infoText.lines().count();

            infoContext.setFill(Color.DARKGRAY);
            infoContext.fillPolygon(new double[]{x, x + 20, x + 20}, new double[]{y, y, y + 20}, 3);
            infoContext.fillRoundRect(x + 10, y, 100, 40 + 15*lines, 20, 20);
            infoContext.setFill(Color.LIGHTGRAY);
            infoContext.fillRoundRect(x + 13, y + 3, 94, 34 + 15*lines, 17, 17);
            infoContext.setFill(Color.DARKGRAY);
            infoContext.fillRect(x + 15, y + 24, 90, 2);
            infoContext.setFill(Color.BLACK);
            infoContext.fillText(loot.getContainer(),x + 20, y + 20, 80);
            infoContext.fillText(infoText, x + 20, y + 40, 80);
        }
    }
}