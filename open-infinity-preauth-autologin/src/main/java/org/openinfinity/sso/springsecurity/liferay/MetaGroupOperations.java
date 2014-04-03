package org.openinfinity.sso.springsecurity.liferay;

abstract class MetaGroupOperations extends GroupOperations {

    private GroupOperations metaGroupParser;


    MetaGroupOperations(LiferayRequest liferayRequest, String groupPattern,
                        GroupOperations metaGroupParser) {
        super(liferayRequest, groupPattern, 2);        
        this.metaGroupParser = metaGroupParser;
    }


    long[] groupIDs() {
        long[] groupIDs = super.groupIDs();
        long[] metaGroupIDs = metaGroupParser.groupIDs();
        /* for (int idx = 0; idx < groupIDs.length; idx++) {
            // TODO check that user is really member of the metagroup
            // TODO that has the group, or does Liferay do this ?
        } */
        return groupIDs;
    }
}
