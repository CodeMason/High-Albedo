/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * This is the method by which ships can move between solar systems.
 */
package celestial;

import celestial.Ship.Ship;
import celestial.Ship.Ship.Autopilot;
import engine.Entity;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.AstralIO;
import universe.SolarSystem;
import universe.Universe;

public class Jumphole extends Planet {

    private Jumphole outGate;
    private final Universe universe;
    protected String out = "n/n";
    private final Random rnd = new Random(1);
    private double flux = 1;

    public Jumphole(String name, Universe universe) {
        super(name, null, 200);
        this.universe = universe;
    }

    @Override
    public void initGraphics() {
        try {
            raw_tex = AstralIO.loadImage("planet/Jumphole.png");
        } catch (NullPointerException | URISyntaxException ex) {
            Logger.getLogger(Jumphole.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void render(Graphics f, double dx, double dy) {
        if (raw_tex != null) {
            int size = (int) (2 * flux * diameter);
            Graphics2D s = (Graphics2D) (f);
            s.drawImage(raw_tex, (int) (getX() - dx) - (size / 2), (int) (getY() - dy) - (size / 2), size, size, null);
        } else {
            initGraphics();
        }
    }

    @Override
    public void alive() {
        //update flux
        flux += (2 * rnd.nextDouble() - 1) * tpf;
        if (flux > 1) {
            flux = rnd.nextDouble();
        } else if (flux < 0.5) {
            flux = rnd.nextDouble();
        }
        //update bound
        getBounds().clear();
        getBounds().add(new Rectangle((int) getX() - getDiameter() / 4, (int) getY() - getDiameter() / 4, getDiameter() / 2, getDiameter() / 2));
        //guarantee link
        if (outGate == null) {
            createLink(out);
        }
    }

    public void createLink(String out) {
        /*
         * Locates this gate's partner in the target solar system.
         */
        String outSysTmp = out.split("/")[0];
        String outGateTmp = out.split("/")[1];
        //find the out link
        for (int a = 0; a < universe.getSystems().size(); a++) {
            SolarSystem curr = universe.getSystems().get(a);
            if (curr.getName().equals(outSysTmp)) {
                for (int b = 0; b < curr.getEntities().size(); b++) {
                    Entity entity = curr.getEntities().get(b);
                    if (entity instanceof Jumphole) {
                        if (entity.getName().equals(outGateTmp)) {
                            outGate = (Jumphole) entity;
                            outGate.linkWithPartner(this);
                        }
                    }
                }
            }
        }
    }

    public void linkWithPartner(Jumphole gate) {
        outGate = gate;
    }

    @Override
    public void informOfCollisionWith(Entity target) {
        if (target instanceof Ship) {
            if (outGate == null) {
                createLink(getOut());
            }
            Ship tmp = (Ship) target;
            if (outGate != null) {
                if (tmp.isPlotShip()) {
                    /*
                     * Plot ships accidentally flying through the wrong jumphole
                     * will cause nothing but problems for the campaign scripts.
                     *
                     * A plot ship can only jump if it is trying to fly to the
                     * jumphole it is coming in contact with.
                     */
                    Celestial flyTo = tmp.getFlyToTarget();
                    if (tmp.getAutopilot() == Autopilot.FLY_TO_CELESTIAL) {
                        if (flyTo == this) {
                            jumpShip(tmp);
                        }
                    }
                } else {
                    jumpShip(tmp);
                }
            }
        }
    }

    private void jumpShip(Ship tmp) {
        tmp.getCurrentSystem().pullEntityFromSystem(tmp);
        tmp.setCurrentSystem(outGate.getCurrentSystem());
        tmp.getCurrentSystem().putEntityInSystem(tmp);
        double dT = Math.atan2(getX() - tmp.getX(), getY() - tmp.getY());
        tmp.setX((outGate.getX() + outGate.getWidth() / 2) + outGate.getDiameter() * Math.cos(dT));
        tmp.setY((outGate.getY() + outGate.getHeight() / 2) + outGate.getDiameter() * Math.sin(dT));
    }

    public String getOut() {
        return out;
    }

    public void setOut(String out) {
        this.out = out;
    }

    public Jumphole getOutGate() {
        return outGate;
    }

    public void setOutGate(Jumphole outGate) {
        this.outGate = outGate;
    }
}
