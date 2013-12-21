@Grab(group='ch.qos.logback', module='logback-classic', version='1.0.13')
import groovy.util.logging.Slf4j

// Define Class
@Slf4j
class Node {
    def id
    def edges = []
    int cost = Integer.MAX_VALUE // 最小コスト(最初は無限大)
    Node from                    // 最小コストはどのノードからきたときか
    boolean done = false         // 評価済みフラグ

    // このノードを評価して隣接ノードのコストに反映する。
    void evalNode() {
        if (done) return
        log.debug "evalNode(): begin: ${this}"
        edges.each { edge ->
            // 各接続先ノードのコストがこのノード経由の方がコストが低いか？
            def toNode = edge.to
            int costToNodeFromThisNode = cost + edge.cost
            if (toNode.cost > costToNodeFromThisNode) {
                // 最小コストを更新する。
                toNode.cost = costToNodeFromThisNode
                toNode.from = this
                log.debug "evalNode(): updated edges node: ${toNode}"
            } else {
                log.debug "evalNode(): ignored edges node: ${toNode}"
            }
        }
        done = true
        log.debug "evalNode(): done: ${this}"
    }

    @Override
    String toString() {
        "${done ? '*' : ' '}node#${id}${edges}:${path}"
    }

    String getPath() {
        (from ? from.path : '') + "/node#${id}(${cost})"
    }

    // グラフ構築のためのヘルパメソッド
    def addEdgeTo(Node toNode, int cost) {
        edges << new Edge(to: toNode, cost: cost)
        this // for method chain
    }
}

class Edge {
    Node to      // 接続先ノード
    int cost = 0 // 接続先ノードへの必要コスト

    @Override
    String toString() {
        "(${cost})->node#${to?.id}"
    }
}

class NodeList {
    @Delegate
    List<Node> nodes = []

    NodeList(int number) {
        number.times {
            nodes[it] = new Node(id: it)
        }
    }

    void calculateCostFrom(startNode) {
        // スタートノードのコストを0に設定する。
        startNode.cost = 0

        // その時点の未評価＆最小コストのノードから順番に評価していく。
        // 最初は未評価＆最小コストはスタートノードしかないので、スタートノードから評価開始される。
        while (true) {
            def node = minCostNode
            if (!node) return
            node.evalNode()
        }
    }

    private getMinCostNode() {
        nodes.findAll { !it.done }.sort { it.cost }.find()
    }

    @Override
    String toString() {
        nodes.collect { it }.join(System.getProperty("line.separator"))
    }
}

// Setup sample data
def newNodeList = {
    def nodes = new NodeList(6)
    nodes[0].addEdgeTo(nodes[1], 5).addEdgeTo(nodes[2], 4).addEdgeTo(nodes[3], 2)
    nodes[1].addEdgeTo(nodes[0], 5).addEdgeTo(nodes[2], 2).addEdgeTo(nodes[5], 6)
    nodes[2].addEdgeTo(nodes[0], 4).addEdgeTo(nodes[1], 2).addEdgeTo(nodes[3], 3).addEdgeTo(nodes[4], 2)
    nodes[3].addEdgeTo(nodes[0], 2).addEdgeTo(nodes[2], 3).addEdgeTo(nodes[4], 6)
    nodes[4].addEdgeTo(nodes[2], 2).addEdgeTo(nodes[3], 6).addEdgeTo(nodes[5], 4)
    nodes[5].addEdgeTo(nodes[1], 6).addEdgeTo(nodes[4], 4)
    return nodes
}

// Solving
(0..5).each { start ->
    def nodes = newNodeList()
    nodes.calculateCostFrom(nodes[start])

    // あるスタートノードに対して全ノードの最小コストが計算できていると、
    // 任意のノードをゴールとしたときの最短経路がわかる。
    // 逆に言えば、あるノードに対する他の全ノードからの最短経路が1回の計算で算出できる。
    // つまり、ゲームなどで言えば、キャラクタの移動先地点をゴールノードではなくスタートノードとすれば
    // 任意の移動元地点からの最短距離が分かるので、複数キャラクタを1地点に移動させるときの計算が1回で済む。
    (0..5).each { goal ->
        println "node#${start}->node#${goal}: " + nodes[goal].path
    }
}

