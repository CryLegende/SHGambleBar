package org.Cry_Legende.sHGambleBar.effect;

import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Formes geometriques disponibles pour les particules.
 * Chaque forme genere une liste de vecteurs relatifs au point d'ancrage.
 */
public enum ParticleShape {

    /**
     * Toutes les particules au point d'ancrage (comportement vanilla).
     */
    POINT,
    /**
     * Cercle horizontal.
     */
    CIRCLE,
    /**
     * Cercle dont le rayon grandit pendant la duree de l'effet.
     */
    RING_EXPANDING,
    /**
     * Sphere pleine repartie uniformement (Fibonacci).
     */
    SPHERE,
    /**
     * Double helice montante.
     */
    HELIX,
    /**
     * Helice simple montante.
     */
    SPIRAL,
    /**
     * Helice dont le rayon se resserre en montant.
     */
    VORTEX,
    /**
     * Explosion : directions aleatoires dans une sphere.
     */
    BURST,
    /**
     * Cone dont le rayon grandit avec la hauteur.
     */
    CONE,
    /**
     * Colonne verticale.
     */
    PILLAR;

    public static ParticleShape parse(String raw, ParticleShape fallback) {
        if (raw == null) return fallback;
        try {
            return valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    /**
     * @param count       nombre de points a generer
     * @param radius      rayon de base
     * @param height      hauteur totale de la forme
     * @param turns       nombre de tours (helices / spirales)
     * @param angleOffset rotation appliquee a l'ensemble (radians)
     * @param progress    avancement de l'animation entre 0.0 et 1.0
     */
    public List<Vector> build(int count, double radius, double height, double turns,
                              double angleOffset, double progress) {
        List<Vector> points = new ArrayList<>(Math.max(1, count));
        ThreadLocalRandom random = ThreadLocalRandom.current();

        switch (this) {
            case POINT -> {
                for (int i = 0; i < count; i++) {
                    points.add(new Vector(0, 0, 0));
                }
            }
            case CIRCLE -> {
                for (int i = 0; i < count; i++) {
                    double angle = (2 * Math.PI * i / count) + angleOffset;
                    points.add(new Vector(Math.cos(angle) * radius, 0, Math.sin(angle) * radius));
                }
            }
            case RING_EXPANDING -> {
                double current = radius * Math.max(0.05, progress);
                for (int i = 0; i < count; i++) {
                    double angle = (2 * Math.PI * i / count) + angleOffset;
                    points.add(new Vector(Math.cos(angle) * current, height * progress, Math.sin(angle) * current));
                }
            }
            case SPHERE -> {
                double golden = Math.PI * (3 - Math.sqrt(5));
                for (int i = 0; i < count; i++) {
                    double y = 1 - (i / (double) Math.max(1, count - 1)) * 2;
                    double ringRadius = Math.sqrt(Math.max(0, 1 - y * y));
                    double theta = golden * i + angleOffset;
                    points.add(new Vector(Math.cos(theta) * ringRadius * radius,
                        y * radius,
                        Math.sin(theta) * ringRadius * radius));
                }
            }
            case HELIX -> {
                int perStrand = Math.max(1, count / 2);
                for (int i = 0; i < perStrand; i++) {
                    double t = i / (double) perStrand;
                    double angle = t * turns * 2 * Math.PI + angleOffset;
                    double y = t * height;
                    points.add(new Vector(Math.cos(angle) * radius, y, Math.sin(angle) * radius));
                    points.add(new Vector(Math.cos(angle + Math.PI) * radius, y, Math.sin(angle + Math.PI) * radius));
                }
            }
            case SPIRAL -> {
                for (int i = 0; i < count; i++) {
                    double t = i / (double) count;
                    double angle = t * turns * 2 * Math.PI + angleOffset;
                    points.add(new Vector(Math.cos(angle) * radius, t * height, Math.sin(angle) * radius));
                }
            }
            case VORTEX -> {
                for (int i = 0; i < count; i++) {
                    double t = i / (double) count;
                    double angle = t * turns * 2 * Math.PI + angleOffset;
                    double currentRadius = radius * (1 - t);
                    points.add(new Vector(Math.cos(angle) * currentRadius, t * height, Math.sin(angle) * currentRadius));
                }
            }
            case BURST -> {
                for (int i = 0; i < count; i++) {
                    double theta = random.nextDouble() * 2 * Math.PI;
                    double phi = Math.acos(2 * random.nextDouble() - 1);
                    double r = radius * Math.cbrt(random.nextDouble());
                    points.add(new Vector(r * Math.sin(phi) * Math.cos(theta),
                        r * Math.cos(phi) + height,
                        r * Math.sin(phi) * Math.sin(theta)));
                }
            }
            case CONE -> {
                for (int i = 0; i < count; i++) {
                    double t = i / (double) count;
                    double angle = t * turns * 2 * Math.PI + angleOffset;
                    points.add(new Vector(Math.cos(angle) * radius * t, t * height, Math.sin(angle) * radius * t));
                }
            }
            case PILLAR -> {
                for (int i = 0; i < count; i++) {
                    double t = i / (double) count;
                    double angle = random.nextDouble() * 2 * Math.PI;
                    double r = random.nextDouble() * radius;
                    points.add(new Vector(Math.cos(angle) * r, t * height, Math.sin(angle) * r));
                }
            }
        }
        return points;
    }
}
