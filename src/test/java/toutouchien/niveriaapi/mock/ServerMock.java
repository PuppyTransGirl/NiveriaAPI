package toutouchien.niveriaapi.mock;

import org.jspecify.annotations.NonNull;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.command.CommandMapMock;

public class ServerMock extends org.mockbukkit.mockbukkit.ServerMock {
    public final CommandMapMock commandMap;

    public ServerMock() {
        super();

        this.commandMap = new CommandMapMock(this);
    }

    @Override
    @NonNull
    public CommandMapMock getCommandMap() {
        return this.commandMap;
    }

    @Override
    public boolean isStopping() {
        return MockBukkit.isMocked();
    }
}
