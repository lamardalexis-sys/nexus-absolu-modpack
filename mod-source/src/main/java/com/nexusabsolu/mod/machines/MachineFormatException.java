package com.nexusabsolu.mod.machines;

/**
 * Levee quand une definition de machine ou la legende est mal formee.
 *
 * Volontairement verifiee (checked) : le chargeur doit decider quoi faire, et
 * la decision est de tracer un message explicite puis de refuser la machine,
 * jamais de la charger a moitie.
 */
public class MachineFormatException extends Exception {

    private static final long serialVersionUID = 1L;

    public MachineFormatException(String message) {
        super(message);
    }

    public MachineFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
