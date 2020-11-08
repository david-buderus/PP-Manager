package model.map.object.room.room;

import model.map.RotationPoint;
import model.map.object.room.RoomObject;
import model.map.object.room.SimpleRoomObject;
import model.map.specification.MapSpecification;
import model.map.specification.texture.TextureHandler;
import ui.map.IMapCanvas;

import java.util.Optional;

public class Entrance extends SimpleRoomObject {

    public Entrance() {
        super(2, 3);
        this.registerExit(new RotationPoint(2, 0, 1, 1));
    }

    @Override
    public void draw(IMapCanvas canvas, TextureHandler textureHandler) {
        canvas.drawRectangle(x, y, z, width, depth, rotation);
    }

    @Override
    public Optional<RoomObject> getFollowingRoomObject(MapSpecification specification, int width) {
        return specification.getPossibleCorridor(width);
    }
}