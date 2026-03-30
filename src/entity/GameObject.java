
package entity;

import java.awt.Graphics;

/*
 OWNER: Jamin

 PURPOSE:
 - Base class for ALL objects

 TASKS:
 1. Add position (x, y)
 2. Add abstract methods:
    - update()
    - render()

 NOTE:
 - All entities MUST extend this
*/

public abstract class GameObject {
    protected int x, y;

    public abstract void update();
    public abstract void render(Graphics g);
}
