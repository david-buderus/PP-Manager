package model.member.state.implementations.other;

import javafx.beans.property.ReadOnlyStringProperty;
import manager.LanguageUtility;
import model.member.BattleMember;
import model.member.state.ActiveRounderMemberState;
import model.member.state.MemberStateIcon;
import model.member.state.interfaces.IActiveRounderMemberState;

public class OtherMemberState extends ActiveRounderMemberState implements IActiveRounderMemberState {


    public OtherMemberState(String name, int duration, boolean activeRounder, BattleMember source) {
        super(name, MemberStateIcon.UNKNOWN, duration, activeRounder, source);
    }

    @Override
    public ReadOnlyStringProperty toStringProperty() {
        return LanguageUtility.getMessageProperty("state.effect.other");
    }
}
