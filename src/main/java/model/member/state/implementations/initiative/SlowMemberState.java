package model.member.state.implementations.initiative;

import javafx.beans.property.ReadOnlyStringProperty;
import manager.LanguageUtility;
import model.member.BattleMember;
import model.member.state.MemberStateIcon;
import model.member.state.PowerMemberState;
import model.member.state.interfaces.IAbsolutInitiativeMemberState;
import model.member.state.interfaces.IPowerMemberState;

public class SlowMemberState extends PowerMemberState implements IPowerMemberState, IAbsolutInitiativeMemberState {

    public SlowMemberState(String name, int duration, boolean activeRounder, BattleMember source, float maxPower) {
        super(name, MemberStateIcon.SLOW, duration, activeRounder, source, maxPower);
    }

    @Override
    public Integer apply(BattleMember member, Integer input) {
        return input - Math.round(getCurrentPower());
    }

    @Override
    public ReadOnlyStringProperty toStringProperty() {
        return LanguageUtility.getMessageProperty("state.effect.slow");
    }
}
