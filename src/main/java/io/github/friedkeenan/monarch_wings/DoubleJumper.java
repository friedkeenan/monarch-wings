package io.github.friedkeenan.monarch_wings;

public interface DoubleJumper {
    public boolean canDoubleJump();

    public boolean isDoubleJumpEnabled();
    public void setDoubleJumpEnabled(boolean double_jumped);

    default public boolean isDoubleJumpDisabled() {
        return !this.isDoubleJumpEnabled();
    }
}
